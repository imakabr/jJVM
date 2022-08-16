package jvm.examples.util;

import java.util.ArrayDeque;

public class ArrayDequeExample {

    public boolean checkAddIsEmptyIntMethod() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            deque.add(i);
        }
        return deque.isEmpty();
    }

    public boolean checkAddPollStringMethod() {
        String expected = "Hello world!";
        ArrayDeque<String> deque = new ArrayDeque<>();
        deque.add(expected);
        return expected.equals(deque.poll()) && deque.isEmpty();
    }

    public boolean checkAddPollIntMethod() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        int[] expected = {0, 1, 2, 3 ,4, 5, 6, 7, 8, 9};
        for (int i = 0; i < 10; i++) {
            deque.add(i);
        }
        int i = 0;
        while (!deque.isEmpty()) {
            if (deque.poll() != expected[i++]) {
                return false;
            }
        }
        return i == 10;
    }

}
