package Chapter8.ReadWriteLock;

import java.util.concurrent.locks.*;

public class FairReadandWriteLock implements ReadWriteLock {
  private final Lock lock = new ReentrantLock(true);
  private final Condition condition = lock.newCondition();

  private int readers = 0;           // 현재 읽기 락 획득 수
  private int writers = 0;           // 현재 쓰기 락 획득 수 (0 or 1)
  private int writeRequests = 0;     // 쓰기 요청 수

  private final Lock readLock = new ReadLock();
  private final Lock writeLock = new WriteLock();

  @Override
  public Lock readLock() {
    return readLock;
  }

  @Override
  public Lock writeLock() {
    return writeLock;
  }

  private class ReadLock implements Lock {
    @Override
    public void lock() {
      lock.lock();
      try {
        // 쓰기 요청이 있거나, 쓰기 중이면 대기
        while (writers > 0 || writeRequests > 0) {
          condition.await();
        }
        readers++;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void unlock() {
      lock.lock();
      try {
        readers--;
        if (readers == 0) {
          condition.signalAll();
        }
      } finally {
        lock.unlock();
      }
    }

    @Override public void lockInterruptibly() throws InterruptedException {
      lock.lockInterruptibly();
      try {
        while (writers > 0 || writeRequests > 0) {
          condition.await();
        }
        readers++;
      } finally {
        lock.unlock();
      }
    }
    @Override public boolean tryLock() {
      lock.lock();
      try {
        if (writers > 0 || writeRequests > 0) return false;
        readers++;
        return true;
      } finally {
        lock.unlock();
      }
    }
    @Override public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException {
      long nanos = unit.toNanos(time);
      lock.lockInterruptibly();
      try {
        while (writers > 0 || writeRequests > 0) {
          if (nanos <= 0) return false;
          nanos = condition.awaitNanos(nanos);
        }
        readers++;
        return true;
      } finally {
        lock.unlock();
      }
    }
    @Override public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }

  private class WriteLock implements Lock {
    @Override
    public void lock() {
      lock.lock();
      try {
        writeRequests++;
        try {
          while (readers > 0 || writers > 0) {
            condition.await();
          }
          writers++;
        } finally {
          writeRequests--;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void unlock() {
      lock.lock();
      try {
        writers--;
        condition.signalAll();
      } finally {
        lock.unlock();
      }
    }

    @Override public void lockInterruptibly() throws InterruptedException {
      lock.lockInterruptibly();
      try {
        writeRequests++;
        try {
          while (readers > 0 || writers > 0) {
            condition.await();
          }
          writers++;
        } finally {
          writeRequests--;
        }
      } finally {
        lock.unlock();
      }
    }
    @Override public boolean tryLock() {
      lock.lock();
      try {
        if (readers > 0 || writers > 0) return false;
        writers++;
        return true;
      } finally {
        lock.unlock();
      }
    }
    @Override public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException {
      long nanos = unit.toNanos(time);
      lock.lockInterruptibly();
      try {
        writeRequests++;
        try {
          while (readers > 0 || writers > 0) {
            if (nanos <= 0) return false;
            nanos = condition.awaitNanos(nanos);
          }
          writers++;
          return true;
        } finally {
          writeRequests--;
        }
      } finally {
        lock.unlock();
      }
    }
    @Override public Condition newCondition() {
      throw new UnsupportedOperationException();
    }
  }
}
