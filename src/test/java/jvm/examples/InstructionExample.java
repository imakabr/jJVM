package jvm.examples;

public class InstructionExample {

    public int calculateSum() {
        int sum = 0;
        for (int i = 1; i<=100; i++) {
            sum += i;
        }
        return sum;
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

    public boolean checkIFGEFalse() {
        int a = 1;
        return a < 0;
    }

    public boolean checkIFGETrue() {
        int a = -1;
        return a < 0;
    }

    public boolean checkIFLETrue() {
        int a = 1;
        return a > 0;
    }

    public boolean checkIFLEFalse() {
        int a = -1;
        return a > 0;
    }

    public boolean checkIFLTTrue() {
        int a = 1;
        return a >= 0;
    }

    public boolean checkIFLTFalse() {
        int a = -1;
        return a >= 0;
    }

    public boolean checkIFGTTrue() {
        int a = -1;
        return a <= 0;
    }

    public boolean checkIFGTFalse() {
        int a = 1;
        return a <= 0;
    }

    public boolean checkIFNETrue() {
        int a = 0;
        return a == 0;
    }

    public boolean checkIFNEFalse() {
        int a = 1;
        return a == 0;
    }

    public boolean checkIFEQTrue() {
        int a = 1;
        return a != 0;
    }

    public int checkINEG() {
        int a = 1;
        return -a;
    }

    public int checkIMUL() {
        int a = 2;
        int b = 3;
        return a * b;
    }

    public int checkISUB() {
        int a = 5;
        int b = 3;
        return a - b;
    }

    public int checkIDIV() {
        int a = 6;
        int b = 3;
        return a / b;
    }

    public int checkIREM() {
        int a = 10;
        int b = 7;
        return a % b;
    }

    public boolean checkIFEQFalse() {
        int a = 0;
        return a != 0;
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

    public int checkISHL() {
        int a = 32;
        return a << 2;
    }

    public int checkISHR() {
        int a = 128;
        return a >> 2;
    }

    public int checkIUSHR() {
        // check negative sign
        int a = -128;
        return a >>> 31;
    }

    public int checkIXOR() {
        int a = 128;
        return ~a;
    }

    public void checkNPEWithIV() {
        String o = null;
        o.length();
    }

    public void checkNPEWhithGetField() {
        ComplexObject complexObject = null;
        SimpleObject simpleObject= complexObject.simpleObject;
    }

    public void checkNPEWhithPutField() {
        ComplexObject complexObject = null;
        complexObject.simpleObject = new SimpleObject();
    }

}
