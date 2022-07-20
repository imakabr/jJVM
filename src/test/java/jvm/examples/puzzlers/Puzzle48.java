package jvm.examples.puzzlers;

public class Puzzle48 {
    public static void main(String[] args) {
        System.out.println(Bark.bark());
    }

    public static boolean test() {
        return "woof woof ".equals(Bark.bark());
    }
}

class Bark {
    public static String bark() {
        Dog2 woofer = new Dog2();
        Dog2 nipper = new Basenji();
        return woofer.bark() + nipper.bark();
    }
}

class Dog2 {
    public static String bark() {
        return "woof ";
    }
}

class Basenji extends Dog2 {
    public static String bark() {
        return "";
    }
}
