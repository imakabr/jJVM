package jvm.examples;

public class ComplexObject {

    private int a;
    public SimpleObject simpleObject;

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


}





