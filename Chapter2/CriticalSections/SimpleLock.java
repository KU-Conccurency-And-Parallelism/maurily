package CriticalSections;

public class SimpleLock implements Lock {

  // 락이 현재 잠겨있는지 아닌지를 나타내는 변수(true면 잠김)
  private boolean isLocked = false;


  // 락을 걸기 위한 메소드 (synchronized는 동시접근을 막기위함 A(1) B(2)가 접근했을떄 A가 먼저 처리되고 B가 처리된다)
  // 동시에 처리되면 critical section에 여러개가 동시에 접근될수 있음
  public synchronized void lock() {
    while (isLocked) {
      try {
        wait();
      } catch (InterruptedException e) {}
    }
    isLocked = true;
  }

  // 락을 해제하기 위한 메소드 (락이 해제된 이후에는 notify로 wait하고 있는 스레드에게 알려 해제)
  public synchronized void unlock() {
    isLocked = false;
    notify();
  }

}
  

