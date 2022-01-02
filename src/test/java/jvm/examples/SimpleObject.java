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

    public int createObjectGetHashCode() {
        Object object = new Object();
        return object.hashCode();
    }

    public int createObjectGetOverriddenHashCode() {
        SimpleObject object = new SimpleObject();
        return object.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        SimpleObject that = (SimpleObject) o;
        return a == that.a && b == that.b && c == that.c;
    }

    @Override
    public int hashCode() {
        return -1555573285;
    }

    @Override
    public String toString() {
        return "I'm SimpleObject, a = " + a + " b = " + b + " c = " + c;
    }

    public static void main(String[] args) {
        System.out.println(m3());
    }
}
