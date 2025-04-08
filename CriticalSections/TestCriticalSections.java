package CriticalSections;

public class TestCriticalSections {
  public static void  main(String[] arg) throws InterruptedException {
    Counter counter = new Counter();

    // 10개의 스레드를 생성하여 Counter의 getAndIncrement() 메소드를 호출
    Thread[] threads = new Thread[10];

    // 스레드 생성 (각 thread가 1000번씩 getAndIncrement() 메소드 호출하도록 명시)
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < 1000; j++) {
          counter.getAndIncrement();
        }
      });
    }

    // 스레드 시작(위에서 명시한 작업 시작)
    for (Thread thread : threads) {
      thread.start();
    }

    // 모든 스레드가 종료될 때까지 대기
    for (Thread thread : threads) {
      thread.join();
    }

    // 최종 카운터 값 출력
    System.out.println("최종 counter 값: " + counter.getValue());

    // 기댓값 : 10000 (10개의 스레드가 각각 1000번씩 getAndIncrement() 메소드를 호출했으므로)
    // 만약 기댓값과 다르다면, 락이 제대로 작동하지 않거나, 스레드 간의 동기화가 잘 이루어지지 않은 것임
    // ex) temp가 100일 때 A랑 B가 동시에 접근하면 A는 temp = 100, B는 temp = 100을 가져와서 102가 되어야 되는데 101이 될 수 있음
  }
}
