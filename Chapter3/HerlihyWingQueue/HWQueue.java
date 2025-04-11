package Chapter3.HerlihyWingQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Array;

// 
public class HWQueue<T> {

  // AtomicReference<T> 배열을 사용해 스레드가 동시 접근해도 경쟁을 못하게 막음
  // A -> B 쓰레드 순서로 접근했을 때 A가 접근하면 B는 null을 받고 다시 시도(Lock-free 방식)
  // CAPACITY는 큐의 최대 크기를 나타내며, 이 값은 필요에 따라 조정 가능하지만 무한히 커질수 있는 한계가 있음
  AtomicReference<T>[] items;
  AtomicInteger tail;
  static final int CAPACITY = 1000;

  @SuppressWarnings("unchecked")
  public HWQueue() {
    // AtomicReference<T> 배열을 생성하고 각 요소를 AtomicReference<T>로 초기화
    items = (AtomicReference<T>[]) Array.newInstance(AtomicReference.class, CAPACITY);
    for (int i = 0; i < items.length; i++) {
      items[i] = new AtomicReference<T>(null);
    }
    // tail을 AtomicInteger로 초기화 (초기값은 0)
    tail = new AtomicInteger(0);
  }

  // enqueue 메서드: 큐에 요소를 추가하는 메서드
  // AtomicInteger를 사용해 tail을 원자적으로 증가시키고, 해당 인덱스에 요소를 추가
  // Lock-free 방식으로 구현되어, 스레드 간의 경쟁을 피할 수 있음
  // Wait-free 방식으로 구현되어, 스레드가 대기하지 않고 즉시 작업을 수행할 수 있음
  public void enq(T x) {
    int i = tail.getAndIncrement();
    items[i].set(x);
  }

  // dequeue 메서드: 큐에서 요소를 제거하는 메서드
  // tail을 원자적으로 가져오고, 해당 인덱스의 요소를 가져온 후 null로 설정
  // Lock-free 방식으로 구현되어, 스레드 간의 경쟁을 피할 수 있음
  // Wait-free 방식으로 구현 X,  스레드가 대기할 수 있음( While(True)문으로 인해 )
  public T deq() {
    while (true) {
      int range = tail.get();
      for (int i = 0; i < range; i++) {
        T value = items[i].getAndSet(null);
        if (value != null) {
          return value;
        }
      }
    }
  }


}