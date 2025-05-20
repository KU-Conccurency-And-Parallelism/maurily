#pragma once

#include <atomic>
#include <chrono>
#include <thread>

class TryCLHLock {
private:
  struct QNode {
    std::atomic<bool> locked = false;
    std::atomic<QNode *> pred = nullptr;
  };

  std::atomic<QNode *> tail;
  thread_local static QNode *myNode;
  thread_local static QNode *myPred;

  static QNode *const AVAILABLE;

public:
  TryCLHLock() {
    auto dummy = new QNode();
    dummy->locked.store(false);
    tail.store(dummy, std::memory_order_relaxed);
  }

  ~TryCLHLock() { delete tail.load(); }

  void lock() {
    myNode->locked.store(true, std::memory_order_relaxed);

    QNode *pred = tail.exchange(myNode, std::memory_order_acq_rel);
    myNode->pred.store(pred, std::memory_order_relaxed);
    myPred = pred;

    while (pred->locked.load(std::memory_order_acquire)) {
      // spin
    }
  }

  bool try_lock_for(std::chrono::milliseconds timeout) {
    auto start = std::chrono::steady_clock::now();

    myNode->locked.store(true, std::memory_order_relaxed);
    QNode *pred = tail.exchange(myNode, std::memory_order_acq_rel);
    myNode->pred.store(pred, std::memory_order_relaxed);
    myPred = pred;

    // -- 여기까지는 lock()과 같음

    // 선임의 선임의 선임의 ...  를 따라가야하는 이유:
    // 맞선임이 중도하차했을 수도 있음
    if (pred == nullptr ||
        pred->pred.load(std::memory_order_acquire) == AVAILABLE)
      return true;

    while (std::chrono::steady_clock::now() - start < timeout) {
      QNode *predPred = pred->pred.load(std::memory_order_acquire);
      if (predPred == AVAILABLE)
        return true;
      if (predPred != nullptr)
        pred = predPred;
      else {
        // 해당 선임 노드에서 무한 대기
      }
    }

    // 시간 초과: 큐에서 빠질 시도
    if (!tail.compare_exchange_strong(myNode, pred,
                                      std::memory_order_acq_rel)) {
      myNode->pred.store(pred, std::memory_order_release);
    }
    return false;
  }

  void unlock() {
    myNode->locked.store(false, std::memory_order_release);
    myNode->pred.store(AVAILABLE, std::memory_order_release);
    myNode = myPred;
  }
};

// 전역 상수 AVAILABLE 노드
thread_local TryCLHLock::QNode *TryCLHLock::myNode = new TryCLHLock::QNode();
thread_local TryCLHLock::QNode *TryCLHLock::myPred = nullptr;
TryCLHLock::QNode *const TryCLHLock::AVAILABLE =
    reinterpret_cast<TryCLHLock::QNode *>(0x1);
