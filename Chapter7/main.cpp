#include "Backoff.h"
#include "CLH.h"
#include "MCS.h"
#include "TAS.h"
#include "TTAS.h"
#include <chrono>
#include <iostream>
#include <thread>
#include <vector>

template <typename LockType>
void benchmark(const std::string &label, int thread_count,
               int increments_per_thread) {
  LockType lock;
  int shared_counter = 0;

  auto start_time = std::chrono::high_resolution_clock::now();

  std::vector<std::thread> threads;
  for (int t = 0; t < thread_count; ++t) {
    threads.emplace_back([&]() {
      for (int i = 0; i < increments_per_thread; ++i) {
        lock.lock();
        ++shared_counter;
        lock.unlock();
      }
    });
  }

  for (auto &th : threads)
    th.join();

  auto end_time = std::chrono::high_resolution_clock::now();
  auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(
                     end_time - start_time)
                     .count();

  std::cout << label << " - Final Counter: " << shared_counter
            << " | Time: " << elapsed << " ms" << std::endl;
}

int main() {
  const int thread_count = 8;
  const int increments_per_thread = 500000;

  benchmark<TASLock>("TASLock", thread_count, increments_per_thread);
  benchmark<TTASLock>("TTASLock", thread_count, increments_per_thread);
  benchmark<BackoffLock>("BackoffLock", thread_count, increments_per_thread);
  benchmark<CLHLock>("CLHLock", thread_count, increments_per_thread);
  benchmark<MCSLock>("MCSLock", thread_count, increments_per_thread);

  return 0;
}
