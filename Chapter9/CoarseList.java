package Chapter9;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseList<T> {
    private Node<T> head;
    private Lock lock = new ReentrantLock();
    
    public CoarseList() {
        head = new Node<T>(Integer.MIN_VALUE);
        head.next = new Node<T>(Integer.MAX_VALUE);
    }

    // for TestLists.java
    public Node<T> accessToHead() {
      return head;
    }
    
    public boolean add(T item) {
        Node<T> pred, curr;
        int key = item.hashCode();
        
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            if (key == curr.key) {
                return false;
            } else {
                Node<T> node = new Node<T>(item);
                node.next = curr;
                pred.next = node;
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(T item) {
        Node<T> pred, curr;
        int key = item.hashCode();
        
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            if (key == curr.key) {
                pred.next = curr.next;
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
}