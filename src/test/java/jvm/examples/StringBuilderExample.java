package jvm.examples;


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

    public int checkStringBuilderAppendChar() {
        char[] actual = new StringBuilder("hello world").append('!').toString().toCharArray();
        char[] expected = "hello world!".toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringBuilderAppendInt() {
        char[] actual = new StringBuilder().append(-123456789).toString().toCharArray();
        char[] expected = "-123456789".toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringBuilderAppendIntMax() {
        char[] actual = new StringBuilder().append(Integer.MAX_VALUE).toString().toCharArray();
        char[] expected = "2147483647".toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringBuilderAppendIntMin() {
        char[] actual = new StringBuilder().append(Integer.MIN_VALUE).toString().toCharArray();
        char[] expected = "-2147483648".toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }
}
