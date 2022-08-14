package jvm.lang;

import jvm.io.PrintStreamJVM;

public class SystemJVM {

    public final static PrintStreamJVM out = new PrintStreamJVM();

    public static void arraycopy(Object src, int srcPos,
                                 Object dest, int destPos,
                                 int length) {
        Object[] s = (Object[]) src;
        Object[] d = (Object[]) dest;
        for (int i = 0; i < length; i++) {
            d[destPos + i] = s[srcPos + i];
        }
    }
}
