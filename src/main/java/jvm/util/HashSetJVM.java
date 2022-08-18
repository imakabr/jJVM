package jvm.util;

import java.util.HashMap;

public class HashSetJVM<E> {

    private final HashMap<E, Object> map;

    private static final Object PRESENT = new Object();

    public HashSetJVM() {
        map = new HashMap<>();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    public void clear() {
        map.clear();
    }
}
