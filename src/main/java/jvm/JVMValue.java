package jvm;

public class JVMValue {

    public final JVMType type;
    public final long value;

    public JVMValue(JVMType t, long bits) {
        this.type = t;
        this.value = bits;
    }

    public static JVMValue entry(int i) {
        return new JVMValue(JVMType.I, i);
    }

}
