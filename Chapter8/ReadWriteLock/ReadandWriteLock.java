package Chapter8.ReadWriteLock;

import java.util.concurrent.locks.*;

public class ReadandWriteLock implements ReadWriteLock {
    private int readers = 0;       // 현재 읽기 락 획득 중인 Reader 수
    private boolean writer = false; // 쓰기 락 보유 여부

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

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
                // 쓰기 중일 때만 대기, 쓰기 대기 중이라도 막지 않음
                while (writer) {
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

        // 사용하지 않는 메서드는 UnsupportedOperationException 처리
        @Override public void lockInterruptibly() throws InterruptedException { throw new UnsupportedOperationException(); }
        @Override public boolean tryLock() { throw new UnsupportedOperationException(); }
        @Override public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException { throw new UnsupportedOperationException(); }
        @Override public Condition newCondition() { throw new UnsupportedOperationException(); }
    }

    private class WriteLock implements Lock {
        @Override
        public void lock() {
            lock.lock();
            try {
                // 다른 쓰기 중이거나 읽기 중이면 대기
                while (writer || readers > 0) {
                    condition.await();
                }
                writer = true;
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
                writer = false;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override public void lockInterruptibly() throws InterruptedException { throw new UnsupportedOperationException(); }
        @Override public boolean tryLock() { throw new UnsupportedOperationException(); }
        @Override public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException { throw new UnsupportedOperationException(); }
        @Override public Condition newCondition() { throw new UnsupportedOperationException(); }
    }
}
