package Chapter9;

public class OptimisticList<T> {
    private Node<T> head;
    
    public OptimisticList() {
        head = new Node<T>(Integer.MIN_VALUE);
        head.next = new Node<T>(Integer.MAX_VALUE);
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
                        return (curr.key == key);
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                pred.unlock();
            }
        }
    }

    private boolean validate(Node<T> pred, Node<T> curr) {
        Node<T> node = head;
        while (node.key <= pred.key) {
            if (node == pred)
                return pred.next == curr;
            node = node.next;
        }
        return false;
    }
}
