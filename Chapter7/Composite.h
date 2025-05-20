#pragma once

#include <atomic>
#include <chrono>
#include <random>
#include <thread>

// TAS + MCS
class CompositeLock {
private:
  struct QNode {
    std::atomic<QNode *> next = nullptr;
    std::atomic<bool> locked = false;
  };

  std::atomic<QNode *> tail = nullptr;
  static constexpr uintptr_t FASTPATH_BIT = 1;

  thread_local static QNode *myNode;

public:
  CompositeLock() = default;

  void lock() {
    QNode *node = myNode;
    node->next.store(nullptr, std::memory_order_relaxed);
    node->locked.store(true, std::memory_order_relaxed);

    // === 빠른 경로 ===
    QNode *expected = nullptr;
    if (tail.compare_exchange_strong(expected,
                                     reinterpret_cast<QNode *>(FASTPATH_BIT),
                                     std::memory_order_acq_rel)) {
      return; // 빠른 경로 성공
    }

    // === 느린 경로 ===
    // & ~FASTPATH_BIT의 안정성을 보장하려고 reinterpret_cast<uintptr_t>함
    // 포인터값은 8바이트임
    QNode *pred =
        reinterpret_cast<QNode *>(reinterpret_cast<uintptr_t>(tail.exchange(
                                      node, std::memory_order_acq_rel)) &
                                  ~FASTPATH_BIT);
    if (pred != nullptr) {
      pred->next.store(node, std::memory_order_release);
      while (node->locked.load(std::memory_order_acquire)) {
        // 지역 스핀
      }
    }
  }

  void unlock() {
    QNode *node = myNode;

    // === 빠른 경로 ===
    QNode *expected = reinterpret_cast<QNode *>(FASTPATH_BIT);
    if (tail.compare_exchange_strong(expected, nullptr,
                                     std::memory_order_acq_rel)) {
      return; //  빠른 경로 성공
    }

    // === 느린 경로 ===
    if (!node->next.load(std::memory_order_acquire)) {
      if (tail.compare_exchange_strong(node, nullptr,
                                       std::memory_order_acq_rel)) {
        return;
      }
      // node->next를 채울때까지 기다림
      while (!node->next.load(std::memory_order_acquire)) {
      }
    }
    node->next.load()->locked.store(false, std::memory_order_release);
  }
};

thread_local CompositeLock::QNode *CompositeLock::myNode =
    new CompositeLock::QNode();
