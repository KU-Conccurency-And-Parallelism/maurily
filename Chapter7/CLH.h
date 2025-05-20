#pragma once

#include <atomic>
#include <thread>

class CLHLock {
private:
  struct QNode {
    std::atomic<bool> locked = false;
  };

  std::atomic<QNode *> tail;         // 공유 tail 포인터
  thread_local static QNode *myNode; // 각 스레드의 현재 노드
  thread_local static QNode *myPred; // 각 스레드의 이전 노드

public:
  // locked false인 더미노드
  CLHLock() { tail.store(new QNode(), std::memory_order_relaxed); }

  ~CLHLock() { delete tail.load(); }

  void lock() {
    myNode->locked.store(true, std::memory_order_relaxed);

    // 나를 끝에 연결하고
    QNode *pred = tail.exchange(myNode, std::memory_order_acq_rel);
    // 선임 정보를 내가 직접 저장
    myPred = pred;

    // 앞 노드가 락을 들고 있는 동안 대기
    while (pred->locked.load(std::memory_order_acquire)) {
      // 로컬 스핀: pred 포인터는 변하지 않음
    }
  }

  void unlock() {
    myNode->locked.store(false, std::memory_order_release);
    // 노드를 재활용
    myNode = myPred;
  }
};

// 각 스레드마다 고유한 QNode 저장
thread_local CLHLock::QNode *CLHLock::myNode = new CLHLock::QNode();
thread_local CLHLock::QNode *CLHLock::myPred = nullptr;
