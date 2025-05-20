#pragma once

#include <atomic>
#include <chrono>
#include <random>
#include <thread>

class BackoffLock {
private:
  std::atomic_flag state = ATOMIC_FLAG_INIT;

  // 마이크로초
  static constexpr int MIN_DELAY = 1;
  static constexpr int MAX_DELAY = 100;

public:
  void lock() {
    int delay = MIN_DELAY;
    std::random_device rd;
    std::mt19937 gen(rd());

    while (true) {
      while (state.test(std::memory_order_relaxed)) {
        // 다른 스레드가 잡고 있다면 local spin
        // MESI 캐시 일관성 모델의 도움을 받아 캐시만 감시하게함
      }

      if (!state.test_and_set(std::memory_order_acquire)) {
        return; // 락 획득 성공
      }

      // 실패했다면 랜덤한 시간만큼 백오프
      std::uniform_int_distribution<> dist(0, delay);
      std::this_thread::sleep_for(std::chrono::microseconds(dist(gen)));

      // 다음엔 지수적으로 딜레이 증가
      delay = std::min(delay * 2, MAX_DELAY);
    }
  }

  void unlock() { state.clear(std::memory_order_release); }
};
