package Chapter3.HerlihyWingQueue;

public class TestHerlihyWingQueue {
  public static void main(String[] args) throws InterruptedException {
    HWQueue<String> queue = new HWQueue<>();

    // 1. 여러 스레드가 enq 수행
    Thread producer1 = new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        queue.enq("P1-" + i);
        System.out.println("Producer1 enqueued: P1-" + i);
      }
    });

    Thread producer2 = new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        queue.enq("P2-" + i);
        System.out.println("Producer2 enqueued: P2-" + i);
      }
    });

    // 2. 여러 스레드가 deq 수행
    Thread consumer1 = new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        String val = queue.deq();
        System.out.println("Consumer1 dequeued: " + val);
      }
    });

    Thread consumer2 = new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        String val = queue.deq();
        System.out.println("Consumer2 dequeued: " + val);
      }
    });

    // 3. 실행 순서: 프로듀서 먼저 시작 → 약간의 딜레이 후 컨슈머 시작
    producer1.start();
    producer2.start();

    producer1.join();
    producer2.join();

    Thread.sleep(100); // 살짝 대기 후 컨슈머 시작

    consumer1.start();
    consumer2.start();

    consumer1.join();
    consumer2.join();

    System.out.println("테스트 완료!");
  }
}
