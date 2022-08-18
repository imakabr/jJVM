package jvm.examples.util;

import java.util.HashSet;

public class HashSetExample {

    public boolean checkPutGetMethod() {
        String[] expected = {"hello", "world", "!!!"};
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < expected.length; i++) {
            set.add(expected[i]);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!set.contains(expected[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPutGetMethod2() {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
        for (int i = 0; i < 100; i++) {
            if (!set.contains(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkClearEmptyMethod() {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
        set.clear();
        return set.isEmpty();
    }
}
