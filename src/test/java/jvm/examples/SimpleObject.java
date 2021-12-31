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

    public int createObjectGetOverriddenHashCode() {
        SimpleObject object = new SimpleObject();
        return object.hashCode();
    }

    public boolean checkDifferentObjectWithEqualsMethod() {
        Object object = new Object();
        Object object2 = new Object();
        return object.equals(object2);
    }

    public boolean checkSameObjectWithEqualsMethod() {
        Object object = new Object();
        Object object2 = object;
        return object.equals(object2);
    }

    public boolean checkSameSimpleObjectWithEqualsMethod() {
        Object object = new SimpleObject(1,2,3);
        Object object2 = new SimpleObject(1,2,3);
        return object.equals(object2);
    }

    public boolean checkSimpleObjectNotEqualsMethod() {
        Object object = new SimpleObject(1,2,3);
        Object object2 = new SimpleObject(1,2,4);
        return object.equals(object2);
    }

    public int checkSimpleCastMethod() {
        Object object = new SimpleObject(1,2,3);
        SimpleObject simpleObject = (SimpleObject) object;
        return simpleObject.a;
    }

    public int checkComplexCastMethod() {
        ChildChildObject object = new ChildChildObject();
        ParentObject simpleObject = (ParentObject) object;
        return simpleObject.a;
    }

    public int checkComplexCastMethod2() {
        Object object = new ParentObject();
        SimpleObject simpleObject = (SimpleObject) object;
        return 1;
    }

    public int checkIFNONNULLFalse() {
        Object object = new SimpleObject(1,2,3);
        if (object == null) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFNONNULLTrue() {
        Object object = null;
        if (object == null) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFNULLTrue() {
        Object object = new SimpleObject(1,2,3);
        if (object != null) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFNULLFalse() {
        Object object = null;
        if (object != null) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFICMPLT() {
        int a = 10;
        int b = 2;
        if (a >= b) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFICMPGT() {
        int a = 2;
        int b = 1;
        if (a <= b) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFICMPGE() {
        int a = 2;
        int b = 10;
        if (a < b) {
            return 1;
        } else {
            return 0;
        }
    }

    public int checkIFICMPLE() {
        int a = 2;
        int b = 10;
        if (a > b) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        SimpleObject that = (SimpleObject) o;
        return a == that.a && b == that.b && c == that.c;
    }

    public int checkIFACMPEQTrue() {
        SimpleObject object = new SimpleObject();
        SimpleObject object2 = new SimpleObject();
        int result = -1;
        if (object != object2) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }

    public int checkIFACMPEQFalse() {
        SimpleObject object = new SimpleObject();
        SimpleObject object2 = object;
        int result = -1;
        if (object != object2) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }

    public int checkIFICMPEQFalse() {
        int a = 5;
        int b = 5;
        int i = -1;
        if (a != b) {
            int c = 2;
            i = c + 243555;
        } else {
            i = 1;
        }
        return i;
    }

    public int checkIFICMPEQTrue() {
        int a = 6;
        int b = 5;
        int i = -1;
        if (a != b) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

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

    public int calculateSum() {
        int sum = 0;
        for (int i = 1; i<=100; i++) {
            sum += i;
        }
        return sum;
    }

    public boolean checkBubbleSorting() {
        int[] array = {5, 34, 56, 567, 23, 89, 73 ,345 ,765 ,14 ,234};
        int[] result = {5, 14, 23, 34, 56, 73, 89, 234, 345, 567, 765};
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                if (array[i] < array[j]) {
                    int temp = array[j];
                    array[j] = array[i];
                    array[i] = temp;
                }
            }
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] != result[i]) {
                return false;
            }
        }
        return true;
    }

    public int checkStringToCharArray() {
        String hello = "hello world";
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        char[] actual = hello.toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringReplace() {
        String hello = "hello to all";
        String str = new String(hello);
        String result = str.replace('l', 'r');
        char[] actual = result.toCharArray();
        char[] expected = "herro to arr".toCharArray();
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expected[i]) {
                return 0;
            }
        }
        return 1;
    }

    public boolean checkStringEqualsTrue() {
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        String first = new String(expected);
        String second = "hello world";
        return first.equals(second);
    }

    public boolean checkStringEqualsFalse() {
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        String first = new String(expected);
        String second = "hallo world";
        return first.equals(second);
    }

    public int checkStringHashCode() {
        String actual = new String("hello world");
        return actual.hashCode();
    }

    @Override
    public int hashCode() {
        return -1555573285;
    }

    public static void main(String[] args) {
        System.out.println(m3());
    }
}
