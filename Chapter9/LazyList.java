package Chapter9;

public class LazyList<T> {
    private Node<T> head;
    
    public LazyList() {
        head = new Node<T>(Integer.MIN_VALUE);
        head.next = new Node<T>(Integer.MAX_VALUE);
    }

    private boolean validate(Node<T> pred, Node<T> curr) { 
        return !pred.marked && !curr.marked && pred.next == curr; 
    }

    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Node<T> pred = head;
            Node<T> curr = pred.next;
            
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key == key) {
                            return false;
                        } else {
                            Node<T> node = new Node<T>(item);
                            node.next = curr;
                            pred.next = node;
                            return true;
                        }
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode(); 
        while (true) { 
            Node<T> pred = head; 
            Node<T> curr = head.next; 
            
            while (curr.key < key) { 
                pred = curr; 
                curr = curr.next; 
            } 
            
            pred.lock(); 
            try { 
                curr.lock(); 
                try { 
                    if (validate(pred, curr)) { 
                        if (curr.key == key) { 
                            curr.marked = true; 
                            pred.next = curr.next; 
                            return true; 
                        } else { 
                            return false; 
                        }
                    } 
                } finally { 
                    curr.unlock(); 
                } 
            } finally { 
                pred.unlock(); 
            } 
        }
    }

    public boolean contains(T item) { 
        int key = item.hashCode(); 
        Node<T> curr = head; 
        
        while (curr.key < key) 
            curr = curr.next; 
            
        return curr.key == key && !curr.marked; 
    }
}
