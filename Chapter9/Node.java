package Chapter9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node<T> {
    T item;                     // 노드가 저장하는 실제 데이터
    int key;                    // item의 고유한 해시 코드 (정렬에 활용)
    Node<T> next;               // 다음 노드를 가리키는 참조
    private Lock lock;          // 노드별 락(for FineList)
    volatile boolean marked = false;  // 논리적 삭제 표시(for LazyList)
    
    // 아이템을 기준으로 생성하는 생성자
    public Node(T item) {
        this.item = item;
        this.key = (item != null) ? item.hashCode() : 0;
        this.lock = new ReentrantLock();
        this.marked = false;
    }
    
    // 키 값만으로 생성하는 생성자 (헤드/테일 노드용)
    public Node(int key) {
        this.key = key;
        this.item = null;
        this.lock = new ReentrantLock();
        this.marked = false;
    }
    
    // 노드에 락을 걸기 위한 메소드(for FineList)
    public void lock() {
        lock.lock();
    }
    
    // 노드의 락을 해제하기 위한 메소드(for FineList)
    public void unlock() {
        lock.unlock();
    }
}