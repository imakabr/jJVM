package jvm.parser;

import jvm.JVMType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class KlassParser {

    private final byte[] clzBytes;
    private final String filename;

    private int major;
    private int minor;

    private int poolItemCount;
    private final ConstantPoolType[] table;
    private int current;

    private int flags;
    private ConstantPoolEntry[] items;
    private int[] interfaces;
    private final Klass klass;

    public KlassParser(byte[] buf, String fName) {
        this.filename = fName;
        this.clzBytes = buf;
        this.klass = new Klass();
        this.table = new ConstantPoolType[20];
        for (ConstantPoolType cp : ConstantPoolType.values()) {
            table[cp.getValue()] = cp;
        }
        parse();
        for (ConstantPoolEntry cpe : items) {
            int classIndex, nameTypeIndex;
            String className, nameAndType;
            switch (cpe.getType()) {
                case FIELDREF:
                    classIndex = cpe.getRef().getOther();
                    className = resolveAsString(classIndex);
                    nameTypeIndex = cpe.getRef2().getOther();
                    nameAndType = resolveAsString(nameTypeIndex);
                    klass.addCPFieldRef(cpe.getIndex(), className + "." + nameAndType);
                    break;
                case METHODREF:
                    classIndex = cpe.getRef().getOther();
                    className = resolveAsString(classIndex);
                    nameTypeIndex = cpe.getRef2().getOther();
                    nameAndType = resolveAsString(nameTypeIndex);
                    klass.addCPMethodRef(cpe.getIndex(), className + "." + nameAndType);
                    break;
                case CLASS:
                    classIndex = cpe.getRef().getOther();
                    className = resolveAsString(classIndex);
                    klass.addCPKlassRef(cpe.getIndex(), className);
                    break;
            }
        }
    }

    private void parse() {
            parseHeader();
            parseConstantPool();
            parseBasicTypeInfo();
            parseFields();
            parseMethods();
    }

    public Klass getKlass() {
        return klass;
    }

    void parseHeader() {
        if ((clzBytes[current++] != (byte) 0xca) || (clzBytes[current++] != (byte) 0xfe)
                || (clzBytes[current++] != (byte) 0xba) || (clzBytes[current++] != (byte) 0xbe)) {
            throw new IllegalArgumentException("Input file does not have correct magic number");
        }
        minor = read2Bytes();
        major = read2Bytes();
        poolItemCount = read2Bytes();
    }

    void parseConstantPool() {
        items = new ConstantPoolEntry[poolItemCount - 1];
        for (short i = 1; i < poolItemCount; i++) {
            int entry = clzBytes[current++] & 0xff;
            ConstantPoolType tag = table[entry];
            if (tag == null) {
                throw new RuntimeException(new ClassNotFoundException("Unrecognised tag byte: " + entry + " encountered at position " + current + ". Stopping the parse."));
            }

            ConstantPoolEntry item = null;
            // Create item based on tag
            switch (tag) {
                case UTF8: // String prefixed by a uint16 indicating the number of bytes in the encoded string which immediately follows
                    int len = read2Bytes();
                    String str = new String(clzBytes, current, len, Charset.forName("UTF8"));
                    item = ConstantPoolEntry.of(i, tag, str);
                    current += len;
                    break;
                case INTEGER: // Integer: a signed 32-bit two's complement number in big-endian format
                    int i2 = read4Bytes();
                    item = ConstantPoolEntry.of(i, tag, i2);
                    break;
                case FLOAT: // Float: a 32-bit single-precision IEEE 754 floating-point number
                    int i3 = read4Bytes();
                    float f = Float.intBitsToFloat(i3);
                    item = ConstantPoolEntry.of(i, tag, f);
                    break;
                case LONG: // Long: a signed 64-bit two's complement number in big-endian format (takes two slots in the constant pool table)
                    int i4 = read4Bytes();
                    int i5 = read4Bytes();
                    long l = ((long) i4 << 32) + (long) i5;
                    item = ConstantPoolEntry.of(i, tag, l);
                    break;
                case DOUBLE: // Double: a 64-bit double-precision IEEE 754 floating-point number (takes two slots in the constant pool table)
                    i4 = read4Bytes();
                    i5 = read4Bytes();
                    l = ((long) i4 << 32) + (long) i5;
                    item = ConstantPoolEntry.of(i, tag, Double.longBitsToDouble(l));
                    break;
                case CLASS: // Class reference: an uint16 within the constant pool to a UTF-8 string containing the fully qualified class name
                case STRING: // String reference: an uint16 within the constant pool to a UTF-8 string
                    int ref = read2Bytes();
                    item = ConstantPoolEntry.of(i, tag, new ConstantPoolRef(ref));
                    break;
                case FIELDREF: // Field reference: two uint16 within the pool, 1st pointing to a Class reference, 2nd to a Name and Type descriptor
                case METHODREF: // Method reference: two uint16s within the pool, 1st pointing to a Class reference, 2nd to a Name and Type descriptor
                case INTERFACE_METHODREF: // Interface method reference: 2 uint16 within the pool, 1st pointing to a Class reference, 2nd to a Name and Type descriptor
                case NAMEANDTYPE: // Name and type descriptor: 2 uint16 to UTF-8 strings, 1st representing a name (identifier), 2nd a specially encoded type descriptor
                    int cpIndex = read2Bytes();
                    int nameAndTypeIndex = read2Bytes();
                    item = ConstantPoolEntry.of(i, tag, new ConstantPoolRef(cpIndex), new ConstantPoolRef(nameAndTypeIndex));
                    break;
                default:
                    throw new RuntimeException(new ClassNotFoundException("Reached impossible Constant Pool Tag."));
            }
            items[i - 1] = item;
        }
    }

    void parseBasicTypeInfo() {
        flags = read2Bytes();
        klass.setKlassName(resolveAsString(read2Bytes()));
        klass.setSuperClassName(resolveAsString(read2Bytes()));
        int count = read2Bytes();
        interfaces = new int[count];
        for (int i = 0; i < count; i++) {
            interfaces[i] = read2Bytes();
        }
    }

    void parseFields() {
        int fCount = read2Bytes();

        for (int idx = 0; idx < fCount; idx++) {
            int fFlags = read2Bytes();
            int name_idx = read2Bytes();
            int desc_idx = read2Bytes();
            int attrs_count = read2Bytes();

            String descriptor = resolveAsString(desc_idx);
            String fieldName = resolveAsString(name_idx) + ":" + descriptor;
            JVMType type;
            if (descriptor.startsWith("L")) {
                type = JVMType.valueOf("A");
            } else {
                type = JVMType.valueOf(descriptor);
            }
            Field field = new Field(fieldName, type, fFlags);
            for (int aidx = 0; aidx < attrs_count; aidx++) {
                parseFieldAttributes(field);
            }
            klass.addField(field);

        }

    }

    private int read2Bytes() {
        return ((int) clzBytes[current++] << 8) + (int) clzBytes[current++];
    }

    private int read4Bytes() {
        return read2Bytes() + read2Bytes();
    }

    void parseMethods() {
        int mCount = read2Bytes();

        for (int idx = 0; idx < mCount; idx++) {
            int mFlags = read2Bytes();
            int name_idx = read2Bytes();
            int desc_idx = read2Bytes();
            int attrs_count = read2Bytes();

            String descriptor = resolveAsString(desc_idx);
            String nameAndType = resolveAsString(name_idx) + ":" + descriptor;
            Method method = new Method(klass.getKlassName(), descriptor, nameAndType, mFlags);
            for (int aidx = 0; aidx < attrs_count; aidx++) {
                parseMethodAttributes(method);
            }

            klass.addDefinedMethod(method);

        }
    }

    void parseFieldAttributes(Field field) {
        int nameCPIdx = read2Bytes();
        int attrLen = read2Bytes() + read2Bytes();
        int endIndex = current + attrLen;

        String attributeType = getConstantPoolEntry(nameCPIdx).getStr();
        current = endIndex;
    }

    void parseMethodAttributes(Method method) {
        int nameCPIdx = read2Bytes();
        int attrLen = read2Bytes() + read2Bytes();
        int endIndex = current + attrLen;
        String s = getConstantPoolEntry(nameCPIdx).getStr();
        if (!"Code".equals(s)) {
            throw new IllegalArgumentException("Must be code");
        }
        method.setMaxStack(read2Bytes());
        method.setMaxLocal(read2Bytes());
        int codeLen = read4Bytes();
        byte[] bytecode = Arrays.copyOfRange(clzBytes, current, current + codeLen);
        method.setBytecode(bytecode);
//    u2 exception_table_length;
//    {   u2 start_pc;
//        u2 end_pc;
//        u2 handler_pc;
//        u2 catch_type;
//    } exception_table[exception_table_length];
//    u2 attributes_count;
//    attribute_info attributes[attributes_count];
        // Skip to the end
        current = endIndex;
    }

    public List<ConstantPoolEntry> getInterfaces() {
        List<ConstantPoolEntry> out = new ArrayList<>();
        for (int i : interfaces) {
            out.add(getConstantPoolEntry(i));
        }
        return out;
    }

    public ConstantPoolEntry getConstantPoolEntry(int i) {
        return items[i - 1]; // CP is 1-based
    }

    public String resolveAsString(int i) {
        final ConstantPoolEntry top = items[i - 1];

        ConstantPoolEntry other = null;
        int left, right = 0;
        switch (top.getType()) {
            case UTF8:
                return top.getStr();
            case INTEGER:
                return "" + top.getNum().intValue();
            case FLOAT:
                return "" + top.getNum().floatValue();
            case LONG:
                return "" + top.getNum().longValue();
            case DOUBLE:
                return "" + top.getNum().doubleValue();
            case CLASS:
            case STRING:
                other = items[top.getRef().getOther() - 1];
                // Verification - could check type is STRING here
                return other.getStr();
            case FIELDREF:
            case METHODREF:
            case INTERFACE_METHODREF:
            case NAMEANDTYPE:
                left = top.getRef().getOther();
                right = top.getRef2().getOther();
                return resolveAsString(left) + top.getType().separator() + resolveAsString(right);
            default:
                throw new RuntimeException("Reached impossible Constant Pool Tag: " + top);
        }
    }

}
