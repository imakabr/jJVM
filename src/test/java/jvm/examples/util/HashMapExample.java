package jvm.examples.util;

import java.util.HashMap;

public class HashMapExample {

    public boolean checkPutGetMethod() {
        String[] expected = {"hello", "world", "!!!"};
        HashMap<Integer, String> map = new HashMap<>();
        for (int i = 0; i < expected.length; i++) {
            map.put(i, expected[i]);
        }
        for (int i = 0; i< expected.length; i++) {
            if (!expected[i].equals(map.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPutGetMethod2() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
        }
        for (int i = 0; i< 100; i++) {
            if (i != map.get(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkContainsMethod() {
        String[] expected = {"hello", "world", "!!!"};
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < expected.length; i++) {
            map.put(expected[i], i);
        }
        return map.containsKey("world");
    }

    public boolean checkClearEmptyMethod() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
        }
        map.clear();
        return map.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println(new HashMapExample().checkPutGetMethod2());
    }
}
