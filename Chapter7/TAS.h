#pragma once

#include <atomic>

class TASLock {
private:
  std::atomic_flag state = ATOMIC_FLAG_INIT;

public:
  void lock() {
    while (state.test_and_set(std::memory_order_acquire)) {
    }
  }

  void unlock() { state.clear(std::memory_order_release); }
};
