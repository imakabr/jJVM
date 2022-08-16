package jvm.util;

import jvm.lang.NullPointerExceptionJVM;

public class ArrayDequeJVM<E> {

    private Object[] elements;
    private int head;
    private int tail;

    public ArrayDequeJVM() {
        elements = new Object[16];
    }

    private void doubleCapacity() {
        int p = head;
        int n = elements.length;
        int r = n - p; // number of elements to the right of p
        int newCapacity = n << 1;
        Object[] a = new Object[newCapacity];
        System.arraycopy(elements, p, a, 0, r);
        System.arraycopy(elements, 0, a, r, p);
        elements = a;
        head = 0;
        tail = n;
    }

    public void addLast(E e) {
        if (e == null) {
            throw new NullPointerExceptionJVM();
        }
        elements[tail] = e;
        if ((tail = (tail + 1) & (elements.length - 1)) == head) {
            doubleCapacity();
        }
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public E poll() {
        return pollFirst();
    }

    public E pollFirst() {
        int h = head;
        @SuppressWarnings("unchecked")
        E result = (E) elements[h];
        // Element is null if deque empty
        if (result == null) {
            return null;
        }
        elements[h] = null;     // Must null out slot
        head = (h + 1) & (elements.length - 1);
        return result;
    }
}
