package jvm.examples;

public class ParentStatic {
    public static int a = 5;
}

class ChildStatic extends ParentStatic {
    public static int b = 10;
}

class ChildChildStatic extends ChildStatic {
    public static int c = 15;
}
