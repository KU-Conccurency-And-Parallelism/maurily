#pragma once

#include <atomic>
#include <thread>

class MCSLock {
private:
  struct QNode {
    std::atomic<QNode *> next = nullptr;
    std::atomic<bool> locked = false;
  };

  std::atomic<QNode *> tail = nullptr;

  thread_local static QNode *myNode;

public:
  void lock() {
    myNode->next.store(nullptr, std::memory_order_relaxed);
    myNode->locked.store(true, std::memory_order_relaxed);

    QNode *pred = tail.exchange(myNode, std::memory_order_acq_rel);

    if (pred != nullptr) {
      // 내가 후임이라고 알림
      pred->next.store(myNode, std::memory_order_release);

      // 선임이 내 값을 바꿔줘야됨
      while (myNode->locked.load(std::memory_order_acquire)) {
        // 완전한 로컬 스핀
      }
    } else {
      // 내가 첫 번째 스레드 → 바로 락 획득
    }
  }

  void unlock() {
    QNode *succ = myNode->next.load(std::memory_order_acquire);

    if (succ == nullptr) {
      // 후임이 아직 없을 수도 있으므로 tail 비우기 시도
      QNode *expected = myNode;
      if (tail.compare_exchange_strong(expected, nullptr,
                                       std::memory_order_acq_rel)) {
        return; // 정말 후임 없음 → 종료
      }

      // 마침 후임이 딱 들어옴

      // 누군가 대기 중이었지만 아직 next 설정 전 → 기다림
      while ((succ = myNode->next.load(std::memory_order_acquire)) == nullptr) {
        // spin
      }
    }

    // 후임이 있으면 locked = false로 넘겨줌
    succ->locked.store(false, std::memory_order_release);
  }
};

thread_local MCSLock::QNode *MCSLock::myNode = new MCSLock::QNode();
