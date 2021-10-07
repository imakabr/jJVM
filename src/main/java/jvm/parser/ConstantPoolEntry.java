package jvm.parser;

public final class ConstantPoolEntry {
    private final short index;
    private final ConstantPoolType type;
    private final Number num;
    private final String str;
    private final ConstantPoolRef ref;
    private final ConstantPoolRef ref2;

    private ConstantPoolEntry(short i, ConstantPoolType t, Number n, String s, ConstantPoolRef r, ConstantPoolRef r2) {
        index = i;
        type = t;
        num = n;
        str = s;
        ref = r;
        ref2 = r2;
    }

    public static ConstantPoolEntry of(short i, ConstantPoolType t, Number num) {
        return new ConstantPoolEntry
                (i, t, num, num.toString(), null, null);
    }

    public static ConstantPoolEntry of(short i, ConstantPoolType t, String s) {
        return new ConstantPoolEntry
                (i, t, null, s, null, null);
    }

    public static ConstantPoolEntry of(short i, ConstantPoolType t, ConstantPoolRef r) {
        return new ConstantPoolEntry
                (i, t, null, null, r, null);
    }

    public static ConstantPoolEntry of(short i, ConstantPoolType t, ConstantPoolRef r, ConstantPoolRef r2) {
        return new ConstantPoolEntry
                (i, t, null, null, r, r2);
    }

    public ConstantPoolType getType() {
        return type;
    }

    public Number getNum() {
        return num;
    }

    public String getStr() {
        return str;
    }

    public ConstantPoolRef getRef() {
        return ref;
    }

    public ConstantPoolRef getRef2() {
        return ref2;
    }

    public short getIndex() {
        return index;
    }

}
