package jvm.examples.puzzlers;

public class Puzzle67 {
    public static boolean test() {
        return "Hello world".equals(new MyString("Hello world").toString());
    }
}


class StrungOut {
    public static void main(String[] args) {
        MyString s = new MyString("Hello world");
        System.out.println(s);
    }
}

class MyString {
    private final String s;

    public MyString(String s) {
        this.s = s;
    }

    public String toString() {
        return s;
    }
}
