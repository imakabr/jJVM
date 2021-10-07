package jvm.parser;

import static jvm.engine.Opcode.*;


public class Method {

    private final String className;
    private final String nameAndType;
    private final String signature;
    private byte[] bytecode;
    private final int flags;
    private int maxStack;
    private int maxLocal;
    private int argSize = -1;

    public int getOperandSize() {
        return maxStack;
    }

    public int getVarSize() {
        return maxLocal;
    }

    private static final byte[] JUST_RETURN = {RETURN.B()};

    public static final Method OBJ_INIT = new Method("java/lang/Object", "()V", "<init>:()V", ParserConstants.ACC_PUBLIC, JUST_RETURN, 0, 0);

    public Method(String klassName, String signature, String nameType, int fls) {
        this(klassName, signature, nameType, fls, null, -1, -1);
    }

    public Method(String klassName, String signature, String nameType, int fls, byte[] buf, int maxStack, int maxLocal) {
        this.signature = signature;
        this.nameAndType = nameType;
        this.flags = fls;
        this.className = klassName;
        this.maxStack = maxStack;
        this.bytecode = buf;
        this.maxLocal = maxLocal;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public void setMaxLocal(int maxLocal) {
        this.maxLocal = maxLocal;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public String getClassName() {
        return className;
    }

    public String getNameAndType() {
        return nameAndType;
    }

    public String getSignature() {
        return signature;
    }

    public int getFlags() {
        return flags;
    }

    public boolean isStatic() {
        return (flags & ParserConstants.ACC_STATIC) > 0;
    }

    public boolean isPrivate() {
        return (flags & ParserConstants.ACC_PRIVATE) > 0;
    }

    public int getArgSize() {
        if (argSize > -1)
            return argSize;
        argSize = 0;
        char[] chars = signature.toCharArray();
        OUTER:
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '(':
                    break;
                case 'Z':
                case 'B':
                case 'S':
                case 'C':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                    argSize++;
                    break;
                case 'L':
                    while (chars[i] != ';') {
                        ++i;
                    }
                    argSize++;
                    break;
                case ')':
                    break OUTER;
                default:
                    throw new IllegalStateException("Saw illegal char: " + c + " in type descriptors");
            }
        }
        return argSize;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    @Override
    public String toString() {
        return "Method{" + "className=" + className + ", nameAndType=" + nameAndType + ", bytecode=" + bytecode + ", signature=" + signature + ", flags=" + flags + ", numParams=" + argSize + '}';
    }


}
