package jvm.engine;

public enum Opcode {
    ACONST_NULL(0x01),
    ALOAD(0x19, 1),
    ALOAD_0(0x2a),
    ALOAD_1(0x2b),
    ALOAD_2(0x2c),
    ALOAD_3(0x2d),
    ARETURN(0xb0),
    ASTORE(0x3a),
    ASTORE_0(0x4b),
    ASTORE_1(0x4c),
    ASTORE_2(0x4d),
    ASTORE_3(0x4e),
    BIPUSH(0x10, 1),
    BREAKPOINT(0xca),
    DADD(0x63),
    DCONST_0(0x0e),
    DCONST_1(0x0f),
    DLOAD(0x18, 1),
    DLOAD_0(0x26),
    DLOAD_1(0x27),
    DLOAD_2(0x28),
    DLOAD_3(0x29),
    DRETURN(0xaf),
    DSTORE(0x39, 1),
    DSTORE_0(0x47),
    DSTORE_1(0x48),
    DSTORE_2(0x49),
    DSTORE_3(0x4a),
    DSUB(0x67),
    DUP(0x59),
    DUP_X1(0x5a),
    GETFIELD(0xb4, 2),
    GETSTATIC(0xb2, 2),
    GOTO(0xa7, 2),
    I2D(0x87),
    I2C(0x92),
    IADD(0x60),
    IAND(0x7e),
    INSTANCEOF(0xc1),
    ISHL(0x78),
    ISHR(0x7a),
    IUSHR(0x7c),
    IXOR(0x82),
    ICONST_M1(0x02),
    ICONST_0(0x03),
    ICONST_1(0x04),
    ICONST_2(0x05),
    ICONST_3(0x06),
    ICONST_4(0x07),
    ICONST_5(0x08),
    IDIV(0x6c),
    IF_ACMPEQ(0xa5, 2),
    IF_ACMPNE(0xa6, 2),
    IF_ICMPEQ(0x9f, 2),
    IF_ICMPNE(0xa0, 2),
    IF_ICMPLT(0xa1, 2),
    IF_ICMPGE(0xa2, 2),
    IF_ICMPGT(0xa3, 2),
    IF_ICMPLE(0xa4, 2),
    IFEQ(0x99, 2),
    IFGE(0x9c, 2),
    IFGT(0x9d, 2),
    IFLE(0x9e, 2),
    IFLT(0x9b, 2),
    IFNE(0x9a, 2),
    CHECKCAST(0xc0, 2),
    IFNONNULL(0xc7, 2),
    IFNULL(0xc6, 2),
    IINC(0x84, 2),
    ILOAD(0x15, 1),
    ILOAD_0(0x1a),
    ILOAD_1(0x1b),
    ILOAD_2(0x1c),
    ILOAD_3(0x1d),
    IMPDEP1(0xfe),
    IMPDEP2(0xff),
    IMUL(0x68),
    INEG(0x74),
    INVOKESPECIAL(0xb7, 2),
    INVOKESTATIC(0xb8, 2),
    INVOKEVIRTUAL(0xb6, 2),
    IOR(0x80),
    IREM(0x70),
    IRETURN(0xac),
    ISTORE(0x36, 1),
    ISTORE_0(0x3b),
    ISTORE_1(0x3c),
    ISTORE_2(0x3d),
    ISTORE_3(0x3e),
    ISUB(0x64),
    MONITORENTER(0xc2),
    MONITOREXIT(0xc3),
    NEW(0xbb, 2),
    NEWARRAY(0xbc, 2),
    ANEWARRAY(0xbd, 2),
    MULTIANEWARRAY(0xc5, 2),
    ARRAYLENGTH(0xbe, 2),
    AALOAD(0x32, 0), //ref array
    BALOAD(0x33, 0), // byte or boolean array
    CALOAD(0x34, 0), // char array
    SALOAD(0x35, 0), // short array
    IALOAD(0x2e, 0), // int array
    LALOAD(0x2f, 0), // long array
    LASTORE(0x50, 0), //long or boolean array
    AASTORE(0x53, 0), //ref array
    BASTORE(0x54, 0), //byte or boolean array
    CASTORE(0x55, 0), //char or boolean array
    SASTORE(0x56, 0), //short or boolean array
    IASTORE(0x4f, 0), //int or boolean array
    JSR(0xa8, 2),
    JSR_W(0xc9, 2),
    LDC(0x12, 1),
    NOP(0x00),
    POP(0x57),
    POP2(0x58),
    PUTFIELD(0xb5, 2),
    PUTSTATIC(0xb3, 2),
    RET(0xa9, 1),
    RETURN(0xb1),
    SIPUSH(0x11, 2),
    SWAP(0x5f);

    public byte getNumParams() {
        return numParams;
    }
    private final int opcode;
    private final byte numParams;

    public int getOpcode() {
        return opcode;
    }

    public byte b() {
        return (byte) opcode;
    }

    Opcode(int b) {
        this(b, 0);
    }

    Opcode(int b, int p) {
        opcode = b;
        numParams = (byte) p;
    }
}
