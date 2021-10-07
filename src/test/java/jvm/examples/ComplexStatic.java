package jvm.examples;

public class ComplexStatic {
    static int a = SimpleStatic.b + SimpleStatic.d;
    static int b = SimpleStatic.c - 127;

    public static int m() {
        return m0() + b;
    }

    public static int m0() {
        return SimpleStatic.m2(SimpleStatic.m0(), SimpleStatic.m1(a));
    }

    public static void main(String[] args) {
        System.out.println(m());
    }
}
