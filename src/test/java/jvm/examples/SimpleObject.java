package jvm.examples;

public class SimpleObject {

    public int a;
    public int b;
    public int c;

    public SimpleObject(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public SimpleObject() {
    }

    public static void m() {
        SimpleObject simpleObject = new SimpleObject(1,2,3);
    }

    public static int m2() {
        return new SimpleObject(34, 23, 78).getSum(5);
    }

    public static int m3() {
        return new SimpleObject(34, 23, 78).getSum(5, 8);
    }

    private int getSum(int i) {
        return a + b + c + i;
    }

    public final int getSum(int i, int j) {
        return a + b + c + i + j;
    }

    public static void main(String[] args) {
        System.out.println(m3());
    }
}
