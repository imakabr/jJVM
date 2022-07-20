package jvm.examples.puzzlers;

public class Puzzle66 {
    public static boolean test() {
        return "Derived".equals(new Derived().getClassName());
    }
}

class Base {
    public String getClassName() {
        return "Base";
    }
}
class Derived extends Base {
    public String getClassName() {
        return "Derived";
    }
}
class PublicMatter {
    public static void main(String[] args) {
        System.out.println(new Derived().getClassName());
    }
}
