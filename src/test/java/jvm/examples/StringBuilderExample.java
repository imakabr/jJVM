package jvm.examples;

import jvm.lang.StringBuilder;

public class StringBuilderExample {

    public int checkStringBuilder() {
        char[] expected = "hello world!".toCharArray();
        char[] actual = new StringBuilder("hello world!").toString().toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringBuilderAppend() {
        char[] expected = "Hello world! I wish you a merry Christmas".toCharArray();
        StringBuilder str = new StringBuilder("Hello")
                .append(" ")
                .append("world!")
                .append(" I ")
                .append("wish ")
                .append("you ")
                .append("a ")
                .append("merry ")
                .append("Christmas");
        char[] actual = str.toString().toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public boolean checkStringBuilderEquals() {
        StringBuilder expected = new StringBuilder("hello ").append("world!");
        StringBuilder actual = new StringBuilder("hello world!");
        return expected.equals(actual);
    }

    public int checkStringBuilderHashCode() {
        return new StringBuilder().append("hello world!").hashCode();
    }
}
