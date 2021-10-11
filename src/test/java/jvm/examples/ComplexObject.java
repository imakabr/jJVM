package jvm.examples;

public class ComplexObject {

    private int a;
    private SimpleObject simpleObject;

    public ComplexObject(int a) {
        this.simpleObject = new SimpleObject(1, 2, 3);
        this.a = a;
    }

    public static int m() {
        ComplexObject complexObject = new ComplexObject(5);
        SimpleObject simpleObject1 = complexObject.getSimpleObject();
        SimpleObject simpleObject2 = new SimpleObject(4, 5, 6);
        return complexObject.evaluate(simpleObject1, simpleObject2);
    }

    private int evaluate(SimpleObject s1, SimpleObject s2) {
        int squareA = getSquare();
        return s1.getSum(squareA, 10) + s2.getSum(a, squareA);

    }

    private int getSquare() {
        return a * a;
    }

    public SimpleObject getSimpleObject() {
        return simpleObject;
    }

    public static void main(String[] args) {
        System.out.println(m());
    }

    public static void m2() {
        ChildComplexObject childComplexObject = new ChildComplexObject(5);
    }
}

class ChildComplexObject extends ComplexObject {

    public ChildComplexObject(int a) {
        super(a);
    }
}

class Print {
    public Print(String str) {
        System.out.println(str);
    }
}

class Near {
    static int a = Far.e;
    static int aa = 6;
    Print b = new Print("non static Near");
    static Print c = new Print("static Near");
    int it;

    public static int nearMethod() {
        return a;
    }


    public int getItNear() {
        return getIt();
    }
    private int getIt() {
        return it;
    }
}
class Far extends Near {
    Print d = new Print("non static Far");
    static int e = 5;
    static Print f = new Print("static Far");
    int getItFar() {
        return super.getItNear();
    }

    public static int farMethod() {
        return e;
    }

    public static void m() {
        Print f = new Far().b;
    }

    public static void main(String[] args) {
        System.out.println(Far.a);
    }
}

class Ex {
    public static void main(String[] args) {
    }
}

