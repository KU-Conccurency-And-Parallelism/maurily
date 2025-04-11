package Chapter2.CriticalSections;

// interface는 다양한 방식의 구현체를 하나의 공통된 형태로 다룰 수 있게 해줌
// 예: lock() 메서드는 SimpleLock, SpinLock, Mutex 등 여러 방식으로 구현될 수 있음
// 이런 다양한 구현을 공통된 인터페이스로 묶으면, 코드 재사용성과 유연성이 높아짐
public interface Lock {
  public void lock();
  public void unlock();
}