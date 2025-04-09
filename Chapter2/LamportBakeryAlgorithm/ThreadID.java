package Chapter2.LamportBakeryAlgorithm;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadID {
    // 다음에 할당할 ID를 추적하는 카운터
    private static AtomicInteger nextID = new AtomicInteger(0);
    
    // 각 스레드에 대한 고유 ID를 저장하는 ThreadLocal 변수
    private static ThreadLocal<Integer> threadID = ThreadLocal.withInitial(() -> nextID.getAndIncrement());
    
    // 최대 스레드 수
    private static int MAX_THREADS = 10; // 필요에 따라 조정
    
    /**
     * 현재 스레드의 ID를 반환합니다.
     */
    public static int get() {
        return threadID.get();
    }
    
    /**
     * 현재 스레드의 ID를 설정합니다.
     */
    public static void set(int id) {
        threadID.set(id);
    }
    
    /**
     * 최대 스레드 수를 반환합니다.
     */
    public static int getMaxThreads() {
        return MAX_THREADS;
    }
    
    /**
     * 최대 스레드 수를 설정합니다.
     */
    public static void setMaxThreads(int maxThreads) {
        MAX_THREADS = maxThreads;
    }
}