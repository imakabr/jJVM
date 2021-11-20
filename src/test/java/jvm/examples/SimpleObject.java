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

    public int createIntArrayAndGetLength() {
        int[] array = new int[13];
        return array.length;
    }

    public int createSimpleObjectArrayAndGetLength() {
        SimpleObject[] simpleObject = new SimpleObject[36];
        return simpleObject.length;
    }

    public int createIntArrayAndGetValueFromIt() {
        int[] array = new int[3];
        array[1] = 122;
        return array[1];
    }

    public int createSimpleObjectArrayAndGetValueFromIt() {
        SimpleObject[] array = new SimpleObject[36];
        array[7] = new SimpleObject(1,2,3);
        SimpleObject simpleObject = array[7];
        return simpleObject.b;
    }

    public int createMultiIntArrayAndGetLength() {
        int[][][] array = new int[1][2][3];
        return array[0][1].length;
    }

    public int createMultiIntArrayAndGetValueFromIt() {
        int[][][][] array = new int[1][2][3][4];
        array[0][1][2][3] = 9;
        return array[0][1][2][3];
    }

    public SimpleObject createMultiSimpleObjectArrayAndGetNullFromIt() {
        SimpleObject[][][][] array = new SimpleObject[1][2][3][4];
        array[0][1][2][3] = null;
        return array[0][1][2][3];
    }

    public int createObjectGetHashCode() {
        Object object = new Object();
        return object.hashCode();
    }

    public static void main(String[] args) {
        System.out.println(m3());
    }
}
