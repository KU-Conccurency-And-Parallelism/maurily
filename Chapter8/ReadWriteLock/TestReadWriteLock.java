package Chapter8.ReadWriteLock;

import java.util.concurrent.locks.Lock;

public class TestReadWriteLock {
    public static void main(String[] args) {
        System.out.println("=== Testing ReadandWriteLock ===");
        testLock(new ReadandWriteLock());

        System.out.println("\n=== Testing FairReadandWriteLock ===");
        testLock(new FairReadandWriteLock());
    }

    public static void testLock(java.util.concurrent.locks.ReadWriteLock rwLock) {
        Lock readLock = rwLock.readLock();
        Lock writeLock = rwLock.writeLock();

        Runnable reader = () -> {
            String name = Thread.currentThread().getName();
            readLock.lock();
            try {
                System.out.println(name + " READ lock 획득");
                Thread.sleep(400);
                System.out.println(name + " READ lock 해제");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                readLock.unlock();
            }
        };

        Runnable writer = () -> {
            String name = Thread.currentThread().getName();
            writeLock.lock();
            try {
                System.out.println(name + " WRITE lock 획득");
                Thread.sleep(600);
                System.out.println(name + " WRITE lock 해제");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
            }
        };

        Thread r1 = new Thread(reader, "Reader-1");
        Thread r2 = new Thread(reader, "Reader-2");
        r1.start();
        r2.start();

        sleep(100);  // Reader 1, 2가 락 잡는 시간 벌기

        Thread w1 = new Thread(writer, "Writer-1");
        w1.start();

        sleep(100);  // Writer가 대기 상태 잡는 시간

        Thread r3 = new Thread(reader, "Reader-3"); // Reader 3는 Writer가 대기 중일 때 실행
        r3.start();

        try {
            r1.join();
            r2.join();
            w1.join();
            r3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("=== Test Finished ===");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
