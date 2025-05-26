package Chapter8.LockedQueue;

public class TestLostWakeups {
  public static void main(String[] args) throws InterruptedException {

    LockedQueue<Integer> queue = new LockedQueue<>(5);

    Runnable consumer = () -> {
      try {
        System.out.println(Thread.currentThread().getName() + " 대기 중...");
        Integer item = queue.deq();
        System.out.println(Thread.currentThread().getName() + ": "+ item + "을 소비");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    Thread consumer1 = new Thread(consumer, "소비자 A");
    Thread consumer2 = new Thread(consumer, "소비자 B");

    consumer1.start();
    consumer2.start();

    Thread.sleep(1000);

    new Thread(() -> {
      try {
        queue.enq(1);
        System.out.println("생산자 C: 1을 생산");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }, "생산자 C").start();

    new Thread(() -> {
      try {
        Thread.sleep(50);
        queue.enq(2);
        System.out.println("생산자 D: 2를 생산");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }, "생산자 D").start();

    Thread.sleep(3000);  // 각 테스트 실행 끝날 때까지 충분히 대기
  }
}
