package jvm.examples.util;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayListExample {

    public int checkStringAddGetMethod() {
        String expected = "Hello world";
        ArrayList<String> list = new ArrayList<>();
        list.add(expected);
        String result = list.get(0);
        for (int i = 0; i < expected.length(); i++) {
            if (expected.charAt(i) != result.charAt(i)) {
                return 0;
            }
        }
        return 1;
    }

    public boolean checkIntAddGetMethod() {
        int expected = 555;
        ArrayList<Integer> list = new ArrayList<>();
        list.add(expected);
        return expected == list.get(0);
    }

    public boolean checkIntsAddGetMethod() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSizeMethod() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        return list.size() == 100;
    }

    public boolean checkClearMethod() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        list.clear();
        return list.size() == 0;
    }

    public boolean checkAddWithIndexMethod() {
        int[] expected = {1 ,2 ,3};
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(1, 2);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetMethod() {
        int[] expected = {1 ,5 ,3};
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.set(1, 5);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean checkRemoveMethod() {
        int[] expected = {1, 3};
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.remove(1);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIsEmptyMethod() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(55);
        list.remove(0);
        return list.isEmpty();
    }

    public boolean checkContainsMethod() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("hello " + i);
        }
        return list.contains("hello 99");
    }

    public boolean checkContainsIntMethod() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        return list.contains(99);
    }

    public boolean checkRemoveObjectMethod() {
        ArrayList<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        list.add("!!!");
        list.remove("world");
        String[] expected = {"hello", "!!!"};
        for (int i = 0; i< list.size(); i++) {
            if (!list.get(i).equals(expected[i])) {
                return false;
            }
        }
        return true;
    }

}
