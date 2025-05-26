package Chapter8.LostWakeupsQueue;

import java.util.concurrent.locks.*;

public class LostWakeupsQueue<T> {
    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    final T[] items;
    int tail, head, count;

    @SuppressWarnings("unchecked")
    public LostWakeupsQueue(int capacity) {
        items = (T[]) new Object[capacity];
    }

    // 문제 있는 enqueue: lost wakeup 가능
    public void enq(T x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[tail] = x;
            if (++tail == items.length) tail = 0;
            ++count;

            if (count == 1) {
                Thread.sleep(200);

                // 이 시점에서 소비자가 대기 중일 수 있음(lost wakeup 가능성)
                notEmpty.signal();

                // 소비자가 대기 중이 아니라면, signalAll을 사용해야 함
                // notEmpty.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public T deq() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            T x = items[head];
            if (++head == items.length) head = 0;
            --count;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}