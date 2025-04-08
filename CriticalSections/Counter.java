package CriticalSections;

public class Counter {

  // 지역 변수 선언 (value 기본값 = 0, lock 기본값 = SimpleLock())
  private long value;
  private Lock lock = new SimpleLock();

  // lock을 이용해 value를 증가시키는 메소드
  public long getAndIncrement() {
    lock.lock();
    try {
      long temp = value;
      value = temp + 1;
      return temp;
    } finally {
      lock.unlock();
    }
  }

  // value를 가져오는 메소드
  public long getValue() {
    return value;
  }
}
