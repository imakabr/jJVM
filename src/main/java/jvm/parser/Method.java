package jvm.parser;


import jvm.engine.Opcode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static jvm.engine.Opcode.values;

public class Method {

    private final String className;
    private final String nameAndType;
    private final String signature;
    private byte[] bytecode;
    private final int flags;
    private int maxStack;
    private int maxLocal;
    private int argSize = -1;
    private String mnemonics;

    public int getOperandSize() {
        return maxStack;
    }

    public int getVarSize() {
        return maxLocal;
    }

    public Method(String klassName, String signature, String nameType, int fls) {
        this(klassName, signature, nameType, fls, null, -1, -1);
    }

    public Method(String klassName, String signature, String nameType, int fls, @Nullable byte[] buf, int maxStack, int maxLocal) {
        this.signature = signature;
        this.nameAndType = nameType;
        this.flags = fls;
        this.className = klassName;
        this.maxStack = maxStack;
        this.bytecode = buf;
        this.maxLocal = maxLocal;
        this.mnemonics = getMnemonics(buf);
    }

    @Nonnull
    private String getMnemonics(@Nullable byte[] byteCode) {
        if (byteCode == null) {
            return "";
        }
        Opcode[] table = new Opcode[256];
        for (Opcode op : values()) {
            table[op.getOpcode()] = op;
        }
        int pointer = 0;
        StringBuilder result = new StringBuilder();
        result.append("| ");
        while (pointer < byteCode.length) {
            if (pointer != 0) {
                result.append(" | ");
            }
            byte b = byteCode[pointer++];
            Opcode op = table[b & 0xff];
            if (op != null) {
                pointer += op.getNumParams();
                result.append(op);
            } else {
                result.append("ERROR");
            }
        }
        result.append(" |");
        return result.toString();
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public void setMaxLocal(int maxLocal) {
        this.maxLocal = maxLocal;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
        this.mnemonics = getMnemonics(bytecode);
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

    public boolean isNative() {
        return (flags & ParserConstants.ACC_NATIVE) > 0;
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
        return "Method{" + "className=" + className + ", nameAndType=" + nameAndType + ", bytecode=" + mnemonics + ", signature=" + signature + ", flags=" + flags + ", numParams=" + argSize + '}';
    }


}
