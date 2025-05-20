#pragma once

#include <atomic>

class TTASLock {
private:
  std::atomic_flag state = ATOMIC_FLAG_INIT;

public:
  std::atomic<int> fail_count = 0;

  void lock() {
    while (true) {
      while (state.test(std::memory_order_relaxed))
        ; // 로컬 스핀
      if (!state.test_and_set(std::memory_order_acquire))
        return;
    }
  }

  void unlock() { state.clear(std::memory_order_release); }
};
