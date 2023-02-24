package jvm.parser;


import jvm.engine.Opcode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
    private final List<DirectRef> directRefList;

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
        this.directRefList = new ArrayList<>();
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
                case '[':
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

    public String getMnemonics() {
        return mnemonics;
    }

    @Override
    public String toString() {
        return "Method{" + "className=" + className + ", nameAndType=" + nameAndType + ", bytecode=" + mnemonics + ", signature=" + signature + ", flags=" + flags + ", numParams=" + argSize + '}';
    }

    public DirectRefBuilder builder() {
        return new DirectRefBuilder();
    }

    @Nonnull
    public DirectRef getDirectRef(int index) {
        return directRefList.get(index);
    }

    public static class DirectRef {
        private final int firstIndex;
        private final int secondIndex;
        @Nonnull
        private final String str;

        public DirectRef(int firstIndex, int secondIndex, @Nonnull String str) {
            this.firstIndex = firstIndex;
            this.secondIndex = secondIndex;
            this.str = str;
        }

        public int getFirstIndex() {
            return firstIndex;
        }

        public int getSecondIndex() {
            return secondIndex;
        }

        @Nonnull
        public String getStr() {
            return str;
        }
    }

    public class DirectRefBuilder {

        private int firstIndex;
        private int secondIndex;
        @Nonnull
        private String str = "";

        public DirectRefBuilder addFirstIndex(int index) {
            this.firstIndex = index;
            return this;
        }

        public DirectRefBuilder addSecondIndex(int index) {
            this.secondIndex = index;
            return this;
        }

        public DirectRefBuilder addString(@Nonnull String str) {
            this.str = str;
            return this;
        }

        public int buildDirectRefIndex() {
            directRefList.add(new DirectRef(firstIndex, secondIndex, str));
            return directRefList.size() - 1;
        }
    }

}
