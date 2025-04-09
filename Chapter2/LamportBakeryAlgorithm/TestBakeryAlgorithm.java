package Chapter2.LamportBakeryAlgorithm;

public class TestBakeryAlgorithm {
    
    // 공유 카운터 클래스 정의
    static class BakeryCounter {
        private int value = 0;
        private final Lock lock;
        
        public BakeryCounter(int threads_count) {
            this.lock = new Bakery(threads_count);
        }
        
        public int getAndIncrement() {
            lock.lock();
            try {
                int temp = value;
                // 스레드 간섭을 더 명확히 확인하기 위한 짧은 지연
                Thread.sleep(1);
                value = temp + 1;
                return temp;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            } finally {
                lock.unlock();
            }
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        final int THREAD_COUNT = 10;  // 테스트할 스레드 수
        final int INCREMENTS_PER_THREAD = 100;  // 각 스레드가 수행할 증가 작업 횟수
        
        // 카운터 객체 생성
        BakeryCounter counter = new BakeryCounter(THREAD_COUNT);
        // 스레드 배열 생성
        Thread[] threads = new Thread[THREAD_COUNT];
        
        // 모든 스레드 생성
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // 스레드 ID 설정
                ThreadID.set(threadId);
                
                // 지정된 횟수만큼 증가 작업 수행
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counter.getAndIncrement();
                }
            }
          );
        }
        
        // 모든 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 모든 스레드 종료 대기
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 최종 카운터 값 출력
        System.out.println("최종 카운터 값: " + counter.getValue());
        System.out.println("기대 값: " + (THREAD_COUNT * INCREMENTS_PER_THREAD));
        
        // 결과 확인
        if (counter.getValue() == THREAD_COUNT * INCREMENTS_PER_THREAD) {
            System.out.println("테스트 성공! 베이커리 알고리즘이 정상 작동합니다.");
        } else {
            System.out.println("테스트 실패! 기대 값과 실제 값이 일치하지 않습니다.");
        }
    }
}