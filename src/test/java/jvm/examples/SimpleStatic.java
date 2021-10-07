package jvm.examples;

public class SimpleStatic {
    static int b = 555;
    static int c = 127;
    static int d = 333;

    public static int m1(int number) {
        return number + d;
    }

    public static int m2(int var1, int var2) {
        return m1(var1) + var2;
    }

    public static int m0() {
        return m2(b, c);
    }

    public static void main(String[] args) {
        System.out.println(m0());
    }
}