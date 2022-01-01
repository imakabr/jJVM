package jvm.examples;

public class ArrayExample {

    public char createCharArray() {
        char[] array = new char[3];
        array[1] = 'f';
        return array[1];
    }

    public boolean createBooleanArray() {
        boolean[] array = new boolean[3];
        array[1] = true;
        boolean result = array[1];
        return result;
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
}
