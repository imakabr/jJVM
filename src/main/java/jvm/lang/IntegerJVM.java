package jvm.lang;

public class IntegerJVM {

    private final int value;

    public IntegerJVM(int value) {
        this.value = value;
    }

    public static IntegerJVM valueOf(int i) {
        return new IntegerJVM(i);
    }

    public int intValue() {
        return value;
    }

    public int hashCode() {
        return value;
    }

    public boolean equals(Object obj) {
        if (obj instanceof IntegerJVM) {
            return value == ((IntegerJVM) obj).value;
        }
        return false;
    }
}
