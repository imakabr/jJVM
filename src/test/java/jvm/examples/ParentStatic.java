package jvm.examples;

public class ParentStatic {
    public static int a = 5;

    public static int parentMethod() {
        return a;
    }
}

class ChildStatic extends ParentStatic {
    public static int b = 10;

    public static int childMethod() {
        return parentMethod() + b + a;
    }
}

class ChildChildStatic extends ChildStatic {
    public static int c = 15;

    public static int childChildMethod() {
        return childMethod() + ParentStatic.parentMethod() + c + a;
    }

    public static void main(String[] args) {
        System.out.println(childChildMethod());
    }
}
