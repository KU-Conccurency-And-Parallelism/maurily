package Chapter9;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestLists {
    
    // 락 획득/해제 로그를 위한 상수
    private static final boolean LOG_LOCKS = true;
    private static final int MAX_LOGS = 100; // 로그 개수 제한
    private static AtomicInteger logCount = new AtomicInteger(0);
    
    // 로그 함수
    private static void logLock(String listType, String action, Object item) {
        if (LOG_LOCKS && logCount.get() < MAX_LOGS) {
            logCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            System.out.println("[" + listType + "] " + threadName + " - " + action + " for item: " + item);
        }
    }
    
    // CoarseList 어댑터
    static class CoarseListAdapter<T> implements Set<T> {
        private final CoarseList<T> list;
        private final String name = "CoarseList";
        
        public CoarseListAdapter() {
            this.list = new LoggingCoarseList<>(name);
        }
        
        @Override
        public boolean add(T x) {
            logLock(name, "Add 시작", x);
            boolean result = list.add(x);
            logLock(name, "Add 종료", x);
            return result;
        }
        
        @Override
        public boolean remove(T x) {
            logLock(name, "Remove 시작", x);
            boolean result = list.remove(x);
            logLock(name, "Remove 종료", x);
            return result;
        }
        
        @Override
        public boolean contains(T x) {
            logLock(name, "Contains 시작", x);
            int key = x.hashCode();
            Node<T> curr = list.accessToHead();
            while (curr != null && curr.key < key)
                curr = curr.next;
            boolean result = curr != null && curr.key == key;
            logLock(name, "Contains 종료", x);
            return result;
        }
    }
    
    // 로그 기능이 추가된 CoarseList 구현
    static class LoggingCoarseList<T> extends CoarseList<T> {
        private final String name;
        
        public LoggingCoarseList(String name) {
            super();
            this.name = name;
        }
        
        @Override
        public boolean add(T item) {
            logLock(name, "락 획득 시도 (add)", item);
            super.lock.lock();
            logLock(name, "락 획득 성공 (add)", item);
            try {
                return super.add(item);
            } finally {
                logLock(name, "락 해제 (add)", item);
                super.lock.unlock();
            }
        }
        
        @Override
        public boolean remove(T item) {
            logLock(name, "락 획득 시도 (remove)", item);
            super.lock.lock();
            logLock(name, "락 획득 성공 (remove)", item);
            try {
                return super.remove(item);
            } finally {
                logLock(name, "락 해제 (remove)", item);
                super.lock.unlock();
            }
        }
    }
    
    // FineList 어댑터
    static class FineListAdapter<T> implements Set<T> {
        private final FineList<T> list;
        private final String name = "FineList";
        
        public FineListAdapter() {
            this.list = new LoggingFineList<>(name);
        }
        
        @Override
        public boolean add(T x) {
            logLock(name, "Add 시작", x);
            boolean result = list.add(x);
            logLock(name, "Add 종료", x);
            return result;
        }
        
        @Override
        public boolean remove(T x) {
            logLock(name, "Remove 시작", x);
            boolean result = list.remove(x);
            logLock(name, "Remove 종료", x);
            return result;
        }
        
        @Override
        public boolean contains(T x) {
            logLock(name, "Contains 시작", x);
            int key = x.hashCode();
            Node<T> curr = list.accessToHead();
            while (curr != null && curr.key < key)
                curr = curr.next;
            boolean result = curr != null && curr.key == key;
            logLock(name, "Contains 종료", x);
            return result;
        }
    }
    
    // 로그 기능이 추가된 FineList 구현
    static class LoggingFineList<T> extends FineList<T> {
        private final String name;
        
        public LoggingFineList(String name) {
            super();
            this.name = name;
        }
        
        @Override
        public boolean add(T item) {
            logLock(name, "head 락 획득 시도 (add)", item);
            return super.add(item);
        }
        
        @Override
        public boolean remove(T item) {
            logLock(name, "head 락 획득 시도 (remove)", item);
            return super.remove(item);
        }
    }
    
    // OptimisticList 어댑터
    static class OptimisticListAdapter<T> implements Set<T> {
        private final OptimisticList<T> list;
        private final String name = "OptimisticList";
        
        public OptimisticListAdapter() {
            this.list = new LoggingOptimisticList<>(name);
        }
        
        @Override
        public boolean add(T x) {
            logLock(name, "Add 시작", x);
            boolean result = list.add(x);
            logLock(name, "Add 종료", x);
            return result;
        }
        
        @Override
        public boolean remove(T x) {
            logLock(name, "Remove 시작", x);
            boolean result = list.remove(x);
            logLock(name, "Remove 종료", x);
            return result;
        }
        
        @Override
        public boolean contains(T x) {
            logLock(name, "Contains 시작", x);
            boolean result = list.contains(x);
            logLock(name, "Contains 종료", x);
            return result;
        }
    }
    
    // 로그 기능이 추가된 OptimisticList 구현
    static class LoggingOptimisticList<T> extends OptimisticList<T> {
        private final String name;
        
        public LoggingOptimisticList(String name) {
            super();
            this.name = name;
        }
        
        @Override
        public boolean add(T item) {
            return super.add(item); // 부모 구현을 사용
        }
        
        @Override
        public boolean remove(T item) {
            return super.remove(item); // 부모 구현을 사용
        }
        
        @Override
        public boolean contains(T item) {
            return super.contains(item); // 부모 구현을 사용
        }
    }
    
    // LazyList 어댑터
    static class LazyListAdapter<T> implements Set<T> {
        private final LazyList<T> list;
        private final String name = "LazyList";
        
        public LazyListAdapter() {
            this.list = new LoggingLazyList<>(name);
        }
        
        @Override
        public boolean add(T x) {
            logLock(name, "Add 시작", x);
            boolean result = list.add(x);
            logLock(name, "Add 종료", x);
            return result;
        }
        
        @Override
        public boolean remove(T x) {
            logLock(name, "Remove 시작", x);
            boolean result = list.remove(x);
            logLock(name, "Remove 종료", x);
            return result;
        }
        
        @Override
        public boolean contains(T x) {
            logLock(name, "Contains 시작", x);
            boolean result = list.contains(x);
            logLock(name, "Contains 종료", x);
            return result;
        }
    }
    
    // 로그 기능이 추가된 LazyList 구현
    static class LoggingLazyList<T> extends LazyList<T> {
        private final String name;
        
        public LoggingLazyList(String name) {
            super();
            this.name = name;
        }
        
        @Override
        public boolean add(T item) {
            return super.add(item); // 부모 구현을 사용
        }
        
        @Override
        public boolean remove(T item) {
            return super.remove(item); // 부모 구현을 사용
        }
        
        @Override
        public boolean contains(T item) {
            return super.contains(item); // 부모 구현을 사용
        }
    }
    
    // 로그 기능이 추가된 Node 클래스
    static class LoggingNode<T> extends Node<T> {
        private final String listType;
        
        public LoggingNode(T item, String listType) {
            super(item);
            this.listType = listType;
        }
        
        public LoggingNode(int key, String listType) {
            super(key);
            this.listType = listType;
        }
        
        @Override
        public void lock() {
            logLock(listType, "노드 락 획득 시도", this.item != null ? this.item : "key=" + this.key);
            super.lock();
            logLock(listType, "노드 락 획득 성공", this.item != null ? this.item : "key=" + this.key);
        }
        
        @Override
        public void unlock() {
            logLock(listType, "노드 락 해제", this.item != null ? this.item : "key=" + this.key);
            super.unlock();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // 테스트 파라미터 설정
        int threadCount = 4; // 로그 보기 쉽게 스레드 수 줄임
        int initialSize = 100; // 초기 크기도 줄임
        int operationsPerThread = 20; // 작업 수도 줄임
        double readRatio = 0.6;
        double insertRatio = 0.2;
        double deleteRatio = 0.2;
        
        // 테스트할 리스트 타입들
        List<TestListInfo<Integer>> testLists = new ArrayList<>();
        testLists.add(new TestListInfo<>("CoarseList", new CoarseListAdapter<>()));
        testLists.add(new TestListInfo<>("FineList", new FineListAdapter<>()));
        testLists.add(new TestListInfo<>("OptimisticList", new OptimisticListAdapter<>()));
        testLists.add(new TestListInfo<>("LazyList", new LazyListAdapter<>()));
        
        System.out.println("=== 병렬 리스트 구현체 락 획득/해제 테스트 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("초기 리스트 크기: " + initialSize);
        System.out.println("스레드당 작업 수: " + operationsPerThread);
        System.out.println("읽기:삽입:삭제 비율 = " + readRatio + ":" + insertRatio + ":" + deleteRatio);
        System.out.println("--------------------------------------");
        
        // 각 리스트 타입에 대해 테스트 수행
        for (TestListInfo<Integer> listInfo : testLists) {
            try {
                System.out.println("\n테스트 대상: " + listInfo.name);
                logCount.set(0); // 로그 카운터 초기화
                
                // 초기 데이터 채우기
                Set<Integer> list = listInfo.list;
                for (int i = 0; i < initialSize; i++) {
                    list.add(i);
                }
                
                // 결과 통계 수집용 카운터
                AtomicInteger successfulContains = new AtomicInteger(0);
                AtomicInteger failedContains = new AtomicInteger(0);
                AtomicInteger successfulInserts = new AtomicInteger(0);
                AtomicInteger failedInserts = new AtomicInteger(0);
                AtomicInteger successfulDeletes = new AtomicInteger(0);
                AtomicInteger failedDeletes = new AtomicInteger(0);
                
                // 모든 스레드가 동시에 시작하도록 배리어 설정
                final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
                
                // 스레드 풀 생성
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                
                // 각 스레드에 작업 할당
                for (int i = 0; i < threadCount; i++) {
                    final int threadId = i;
                    executor.submit(() -> {
                        Thread.currentThread().setName("Thread-" + threadId);
                        try {
                            barrier.await();
                            
                            // 작업 수행
                            for (int op = 0; op < operationsPerThread; op++) {
                                try {
                                    double chance = Math.random();
                                    int key = (int) (Math.random() * initialSize * 2);
                                    
                                    if (chance < readRatio) {
                                        // 읽기 연산
                                        boolean found = list.contains(key);
                                        if (found) successfulContains.incrementAndGet();
                                        else failedContains.incrementAndGet();
                                    } else if (chance < readRatio + insertRatio) {
                                        // 삽입 연산
                                        boolean added = list.add(key);
                                        if (added) successfulInserts.incrementAndGet();
                                        else failedInserts.incrementAndGet();
                                    } else {
                                        // 삭제 연산
                                        boolean removed = list.remove(key);
                                        if (removed) successfulDeletes.incrementAndGet();
                                        else failedDeletes.incrementAndGet();
                                    }
                                    
                                    // 연산 사이에 약간의 지연을 주어 로그를 보기 쉽게 함
                                    Thread.sleep(10);
                                } catch (Exception e) {
                                    System.err.println("작업 중 오류 발생: " + e.getMessage());
                                }
                            }
                            
                            barrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                
                barrier.await();
                barrier.await();
                
                // 스레드 풀 종료
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.SECONDS);
                
                // 결과 출력
                System.out.println("\n*** " + listInfo.name + " 결과 요약 ***");
                System.out.println("읽기 성공/실패: " + successfulContains.get() + "/" + failedContains.get());
                System.out.println("삽입 성공/실패: " + successfulInserts.get() + "/" + failedInserts.get());
                System.out.println("삭제 성공/실패: " + successfulDeletes.get() + "/" + failedDeletes.get());
                System.out.println("--------------------------------------");
            } catch (Exception e) {
                System.err.println(listInfo.name + " 테스트 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // 테스트할 리스트 정보를 담는 클래스
    static class TestListInfo<T> {
        String name;
        Set<T> list;
        
        TestListInfo(String name, Set<T> list) {
            this.name = name;
            this.list = list;
        }
    }
}