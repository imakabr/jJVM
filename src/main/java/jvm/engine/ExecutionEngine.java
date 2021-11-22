package jvm.engine;

import jvm.JVMType;
import jvm.heap.*;
import jvm.parser.Method;
import jvm.parser.Klass;
import jvm.parser.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jvm.engine.Opcode.*;
import static jvm.heap.KlassLoader.*;

public final class ExecutionEngine {

    private final static int NULL = 0;

    private final Opcode[] table = new Opcode[256];
    private final Heap heap;

    public ExecutionEngine(Heap heap) {
        this.heap = heap;
        for (Opcode op : values()) {
            table[op.getOpcode()] = op;
        }
    }

    public long invoke(Method firstMethod) {

        int programCounter = 0;
        Method[] stackMethod = new Method[100];
        int stackMethodPointer = 0;
        stackMethod[0] = firstMethod;
        byte[] byteCode = firstMethod.getBytecode();
        String klassName = firstMethod.getClassName();

        StackFrame stack = new StackFrame(firstMethod.getVarSize(), firstMethod.getOperandSize());
//        Heap heap = HeapHolder.getHeap();
        while (true) {
            byte b = byteCode[programCounter++];
            Opcode op = table[b & 0xff];
            if (op == null) {
                throw new RuntimeException("Unrecognised opcode byte: " + (b & 0xff) + " encountered at position " + (programCounter - 1) + ". Stopping.");
            }
            byte num = op.numParams();
            int jumpTo;

            InstanceObject object;
            Method method;
            long reference;
            long value;
            int first;
            int second;
            int cpLookup;
            int objectRef;
            int fieldValueIndex;
            int methodIndex;
            switch (op) {
                case ACONST_NULL:
                    // push the null object reference onto the operand stack
                    stack.push(setRefValueType(NULL));
                    break;
                case ALOAD:
                    // The objectref in the local variable at index is pushed onto the operand stack
                    reference = stack.getLocalVar(byteCode[programCounter++]);
                    stack.push(reference);
                    break;
                case ALOAD_0:
                    reference = stack.getLocalVar(0);
                    stack.push(reference);
                    break;
                case ALOAD_1:
                    reference = stack.getLocalVar(1);
                    stack.push(reference);
                    break;
                case ALOAD_2:
                    reference = stack.getLocalVar(2);
                    stack.push(reference);
                    break;
                case ALOAD_3:
                    reference = stack.getLocalVar(3);
                    stack.push(reference);
                    break;
                case ARETURN:
                    if (stack.invokeCount == 0) {
                        return getPureValue(checkValueType(JVMType.A, stack.pop()));
                    }
                    method = stackMethod[--stackMethodPointer];
                    stackMethod[stackMethodPointer + 1] = null;
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), true);
                    programCounter = stack.programCounter;
                    break;
                case ASTORE_0:
                    reference = stack.pop();
                    stack.setLocalVar(0, reference);
                    break;
                case ASTORE_1:
                    reference = stack.pop();
                    stack.setLocalVar(1, reference);
                    break;
                case ASTORE_2:
                    reference = stack.pop();
                    stack.setLocalVar(2, reference);
                    break;
                case BIPUSH:
                    //The immediate byte is sign-extended to an int value. That value is pushed onto the operand stack.
                    stack.push(setIntValueType(byteCode[programCounter++]));
                    break;
                case DUP:
                    //Duplicate the top operand stack value
                    stack.dup();
                    break;
                case DUP_X1:
                    stack.dupX1();
                    break;
                //------------------------------------------------------------------------------------------------------------------------
                case GETFIELD:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                    stack.push(object.getValue(fieldValueIndex));
                    break;
                case GETSTATIC:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    //---------------------------------------------------------------------------------

                    // todo restore indexes for resolving
                    objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                    fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));

                    //---------------------------------------------------------------------------------
                    stack.push(heap.getInstanceObject(objectRef).getValue(fieldValueIndex)); // to do deal with type to JVMValue
                    break;
                //----------------------------------------------------------------------------------------------------------------------
                case GOTO:
                    programCounter += (((int) byteCode[programCounter] << 8) + (int) byteCode[programCounter + 1]) - 1;
                    break;
                case IADD:
                    stack.push(setIntValueType(getPureValue(stack.pop()) + getPureValue(stack.pop())));
                    break;
                case IAND:
                    stack.push(stack.pop() & stack.pop());
                    break;
                case ICONST_0:
                    stack.push(setIntValueType(0));
                    break;
                case ICONST_1:
                    stack.push(setIntValueType(1));
                    break;
                case ICONST_2:
                    stack.push(setIntValueType(2));
                    break;
                case ICONST_3:
                    stack.push(setIntValueType(3));
                    break;
                case ICONST_4:
                    stack.push(setIntValueType(4));
                    break;
                case ICONST_5:
                    stack.push(setIntValueType(5));
                    break;
                case ICONST_M1:
                    stack.push(setIntValueType(-1));
                    break;
                case IDIV:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    if (second == 0) throw new ArithmeticException("cannot divide 0");
                    stack.push(second / first);
                    break;
                case IF_ACMPNE:
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (getPureValue(checkValueType(JVMType.A, stack.pop()))
                            != getPureValue(checkValueType(JVMType.A, stack.pop()))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ACMPEQ:
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (getPureValue(checkValueType(JVMType.A, stack.pop()))
                            == getPureValue(checkValueType(JVMType.A, stack.pop()))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPEQ:
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (checkValueType(JVMType.I, stack.pop()) == checkValueType(JVMType.I, stack.pop())) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFEQ:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first == 0) {
                        programCounter += jumpTo - 1; // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;
                case IFGE:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first >= 0) {
                        programCounter += jumpTo - 1; // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;
                case IFGT:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first > 0) {
                        programCounter += jumpTo - 1; // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;
                case IFLE:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first <= 0) {
                        programCounter += jumpTo - 1; // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;
                case IFLT:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first < 0) {
                        programCounter += jumpTo - 1; // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;
                case IFNE:
                    first = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter] << 8) + (int) byteCode[programCounter + 1];
                    if (first != 0) {
                        programCounter += jumpTo - 1;  // The -1 is necessary as we've already inc'd programCounter
                    }
                    break;

                //---------------------------------------------------------------------------------------------------------------------------
                case IFNONNULL:
                    break;
                case IFNULL:
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------
                case IINC:
                    // Increment local variable by constant
                    int index = byteCode[programCounter++];
                    first = (int) stack.getLocalVar(index);
                    stack.setLocalVar(index, first + byteCode[programCounter++]);
                    break;
                case ILOAD:
                    //Load int from local variable to the operand stack
                    stack.push(stack.getLocalVar(byteCode[programCounter++]));
                    break;
                case ILOAD_0:
                    stack.push(stack.getLocalVar(0));
                    break;
                case ILOAD_1:
                    stack.push(stack.getLocalVar(1));
                    break;
                case ILOAD_2:
                    stack.push(stack.getLocalVar(2));
                    break;
                case ILOAD_3:
                    stack.push(stack.getLocalVar(3));
                    break;
                case IMUL:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push((long) first * second);
                    break;
                case INEG:
                    first = (int) stack.pop();
                    stack.push(-first);
                    break;
                //------------------------------------------------------------------------------------------------------------------------------------------
                case INVOKESPECIAL:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    methodIndex = getMethodIndex(klassName, cpLookup); // todo restore to resolution
                    method = heap.getMethodRepo().getMethod(methodIndex);
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stackMethod[++stackMethodPointer] = method;
                    stack.programCounter = programCounter;
                    programCounter = 0;
                    stack.initNewMethodStack(method.getArgSize() + 1, method.getVarSize(), method.getOperandSize());
                    break;
                case INVOKESTATIC:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    methodIndex = getStaticMethodIndex(klassName, cpLookup); // todo restore to resolution
                    method = heap.getMethodRepo().getMethod(methodIndex);
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stackMethod[++stackMethodPointer] = method;
                    stack.programCounter = programCounter;
                    programCounter = 0;
                    stack.initNewMethodStack(method.getArgSize(), method.getVarSize(), method.getOperandSize());
                    break;
                case INVOKEVIRTUAL:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    int klassIndex = heap.getInstanceObject(
                                    getPureValue(
                                            checkValueType(
                                                    JVMType.A,
                                                    stack.getObjectRefBeforeInvoke(getArgSize(klassName, cpLookup)))))
                            .getKlassIndex();
                    int virtualMethodIndex = getVirtualMethodIndex(klassName, cpLookup, klassIndex); // todo restore to resolution
                    methodIndex = heap.getInstanceKlass(klassIndex).getMethodIndex(virtualMethodIndex);

                    method = heap.getMethodRepo().getMethod(methodIndex);
                    if (!method.isNative()) {
                        byteCode = method.getBytecode();
                        klassName = method.getClassName();
                        stackMethod[++stackMethodPointer] = method;
                        stack.programCounter = programCounter;
                        programCounter = 0;
                        stack.initNewMethodStack(method.getArgSize() + 1, method.getVarSize(), method.getOperandSize());
                    } else {
                        invokeNativeMethod(stack, method);
                    }
                    break;
                //------------------------------------------------------------------------------------------------------------------------------------------
                case IOR:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push(first | second);
                    break;
                case IREM:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push(second % first);
                    break;
//                    ----------------------------------------------------------------------------------------------------------------------------------
                case IRETURN:
                    if (stack.invokeCount == 0) {
                        return getPureValue(stack.pop());
                    }
                    method = stackMethod[--stackMethodPointer];
                    stackMethod[stackMethodPointer + 1] = null;
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), true);
                    programCounter = stack.programCounter;
                    break;
//                    ----------------------------------------------------------------------------------------------------------------------------------
                case ISTORE:
                    stack.setLocalVar(byteCode[programCounter++], stack.pop());
                    break;
                case ISTORE_0:
                    stack.setLocalVar(0, stack.pop());
                    break;
                case ISTORE_1:
                    stack.setLocalVar(1, stack.pop());
                    break;
                case ISTORE_2:
                    stack.setLocalVar(2, stack.pop());
                    break;
                case ISTORE_3:
                    stack.setLocalVar(3, stack.pop());
                    break;
                case ISUB:
                    first = getPureValue(stack.pop());
                    second = getPureValue(stack.pop());
                    stack.push(setIntValueType(first - second));
                    break;

                //--------------------------------------------------------------------------------------------------------------------------------------
                case MONITORENTER:
                case MONITOREXIT:
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case NEW:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    stack.push(setRefValueType(allocateInstanceObjectAndGetReference(klassName, cpLookup)));
                    break;
                case NEWARRAY:
                    int atype = byteCode[programCounter++];
                    /*  Array Type	atype
                        T_BOOLEAN	4
                        T_CHAR	    5
                        T_FLOAT	    6
                        T_DOUBLE	7
                        T_BYTE	    8
                        T_SHORT	    9
                        T_INT	    10
                        T_LONG	    11
                        */
                    stack.push(setRefValueType(allocateArray(
                            JVMType.values()[atype - 4].name(),
                            getPureValue(stack.pop()))));
                    break;
                case ANEWARRAY:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    stack.push(setRefValueType(allocateArrayOfRef(
                            klassName,
                            cpLookup,
                            getPureValue(stack.pop()))));
                    break;
                case MULTIANEWARRAY:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    int[] dimensions = new int[byteCode[programCounter++]];
                    for (int i = dimensions.length - 1; i >= 0; i--) {
                        dimensions[i] = getPureValue(checkValueType(JVMType.I, stack.pop()));
                    }
                    object = allocateArrayOfRef(dimensions[0], -1);
                    stack.push(setRefValueType(heap.getObjectRef(object)));
                    createMultiArray(1, dimensions, object, getValueType(klassName, cpLookup));
                    break;
                case ARRAYLENGTH:
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    checkArrayObject(object);
                    stack.push(setIntValueType(object.size()));
                    break;
                case AALOAD:
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    checkArrayObject(object);
                    stack.push(checkValueType(JVMType.A, object.getValue(index)));
                    break;
                case IALOAD:
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    checkArrayObject(object);
                    stack.push(checkValueType(JVMType.I, object.getValue(index)));
                    break;
                case BALOAD:
                    break;
                case CALOAD:
                    break;
                case AASTORE:
                    value = checkValueType(JVMType.A, stack.pop());
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    checkArrayObject(object);
                    object.setValue(index, value);
                    break;
                case IASTORE:
                    value = checkValueType(JVMType.I, stack.pop());
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    checkArrayObject(object);
                    object.setValue(index, value);
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case NOP:
                    break;
                case POP:
                    stack.pop();
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case POP2:
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case PUTFIELD:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];

                    value = stack.pop();
                    object = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
                    fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                    object.setValue(fieldValueIndex, value);
                    break;
                case PUTSTATIC:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    //---------------------------------------------------------------------------------
                    // todo restore indexes for resolving
                    objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                    fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));
                    //----------------------------------------------------------------------------------
                    heap.getInstanceObject(objectRef)
                            .setValue(fieldValueIndex, stack.pop()); // to do create type to JVMValue
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case RET:
                    throw new IllegalArgumentException("Illegal opcode byte: " + (b & 0xff) + " encountered at position " + (programCounter - 1) + ". Stopping.");
                case RETURN:
                    if (stackMethodPointer == 0) {
                        return 0;
                    }
                    method = stackMethod[--stackMethodPointer];
                    stackMethod[stackMethodPointer + 1] = null;
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), false);
                    programCounter = stack.programCounter;
                    break;
                //-----------------------------------------------------------------------------------------------------------------------------------------------
                case SIPUSH:
                    stack.push(setIntValueType(((int) (byteCode[programCounter++]) << 8) + (byteCode[programCounter++] & 0xff)));
                    break;
                case SWAP:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push(first);
                    stack.push(second);
                    break;
                //-------------------------------------------------------------------------------------------------------------------------------------
                case LDC:
                    cpLookup = byteCode[programCounter++];
                    ConstantPoolEntry entry = heap.getKlassLoader().getLoadedKlassByName(klassName).getCPItem(cpLookup - 1);
                    switch (entry.getType()) {
                        case INTEGER:
                            stack.push(setIntValueType((Integer) entry.getNum()));
                            break;
                        case STRING:
                            // todo implement String object
                            break;
                    }
                    break;
                //-------------------------------------------------------------------------------------------------------------------------------------
                case BREAKPOINT:
                case IMPDEP1:
                case IMPDEP2:
                case JSR:
                case JSR_W:
                default:
                    System.err.println("Saw " + op + " - that can't happen. Stopping.");
                    System.exit(1);
            }
        }
    }

    private void invokeNativeMethod(StackFrame stack, Method method) {
        if ("hashCode:()I".equals(method.getNameAndType())) {
            InstanceObject object1 = heap.getInstanceObject(getPureValue(checkValueType(JVMType.A, stack.pop())));
            stack.push(setIntValueType(Objects.hashCode(object1)));
        }
    }

    private void createMultiArray(int indexDim, int[] dimensions, InstanceObject object, String type) {
        for (int i = 0; i < object.size(); i++) {
            if (indexDim == dimensions.length - 1) {
                object.setValue(i, setRefValueType(allocateArray(type, dimensions[indexDim])));
            } else {
                InstanceObject newObject = allocateArrayOfRef(dimensions[indexDim], -1);
                object.setValue(i, setRefValueType(heap.getObjectRef(newObject)));
                createMultiArray(indexDim + 1, dimensions, newObject, type);
            }
        }
    }

    private long checkValueType(JVMType type, long value) {
        if (getValueType(value) != type.ordinal()) {
            throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(value)] + " is not equal " + type.name());
        }
        return value;
    }

    private void checkArrayObject(InstanceObject object) {
        if (!object.isArray()) {
            throw new RuntimeException("Object is not array");
        }
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    private String getValueType(String klassName, int cpLookup) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(klassName);
        String des = sourceKlass.getKlassNameByCPIndex((short) cpLookup);
        return des.substring(des.lastIndexOf('[') + 1);
    }

    private long setIntValueType(long value) {
        return setValueType(JVMType.I.ordinal()) ^ value;
    }

    private long setRefValueType(long value) {
        return setValueType(JVMType.A.ordinal()) ^ value;
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getPureValue(long value) {
        return (int) value;
    }

    private String getName(String fullName) {
        return fullName.substring(fullName.indexOf(".") + 1);
    }

    private String getFieldName(String klassFieldName) {
        return getName(klassFieldName);
    }

    private String getMethodName(String klassMethodName) {
        return getName(klassMethodName);
    }

    private String getKlassName(String fullName) {
        return fullName.substring(0, fullName.indexOf("."));
    }

    private int getStaticFieldIndex(String klassFieldName) {
        return heap.getInstanceKlass(getInstanceKlassIndex(klassFieldName))
                .getIndexByFieldName(getFieldName(klassFieldName));
    }

    private int getInstanceKlassIndex(String fullName) {
        return heap.getKlassLoader().getInstanceKlassIndexByName(getKlassName(fullName), true);
    }

    private String getKlassFieldName(String sourceKlassName, int cpLookup) {
        Klass loadedKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        return loadedKlass.getFieldByCPIndex((short) cpLookup);
    }

    private int getMethodIndex(String sourceKlassName, int cpIndex) {
        Klass klass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String methodName = klass.getMethodNameByCPIndex((short) cpIndex);
        return heap.getMethodRepo().getIndexByName(methodName);
    }

    private int getStaticMethodIndex(String sourceKlassName, int cpIndex) {
        Klass klass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String klassMethodName = klass.getMethodNameByCPIndex((short) cpIndex);
        InstanceKlass instanceKlass = heap.getInstanceKlass(getInstanceKlassIndex(klassMethodName));
        return instanceKlass.getIndexByMethodName(getMethodName(klassMethodName));
    }

    private int getVirtualMethodIndex(String sourceKlassName, int cpIndex, int klassIndex) {
        Klass klass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String fullName = klass.getMethodNameByCPIndex((short) cpIndex);
        InstanceKlass instanceKlass = heap.getInstanceKlass(klassIndex);
        return instanceKlass.getIndexByVirtualMethodName(getMethodName(fullName));
    }

    private int getArgSize(String sourceKlassName, int cpIndex) {
        Klass klass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String fullName = klass.getMethodNameByCPIndex((short) cpIndex);
        return heap.getInstanceKlass(getInstanceKlassIndex(fullName))
                .getCpKlass()
                .getMethodByName(getMethodName(fullName))
                .getArgSize();
    }

    private int allocateInstanceObjectAndGetReference(String sourceKlassName, int cpIndex) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String destKlassName = sourceKlass.getKlassNameByCPIndex((short) cpIndex);
        Klass destKlass = heap.getKlassLoader().getLoadedKlassByName(destKlassName);
        List<Klass> klasses = new ArrayList<>();
        Klass current = destKlass;
        klasses.add(current);
        while (!ABSENCE.equals(current.getParent())) {
            current = heap.getKlassLoader().getLoadedKlassByName(current.getParent());
            klasses.add(current);
        }
        List<String> fields = new ArrayList<>();
        for (int i = klasses.size() - 1; i >= 0; i--) {
            fields.addAll(klasses.get(i).getObjectFieldNames());
        }

        return heap.getObjectRef(new InstanceObject(fields,
                heap.getKlassLoader().getInstanceKlassIndexByName(destKlassName, false)));
    }


    private long allocateArray(String type, int count) {
        int klassIndex = -1;
        if (type.startsWith("L")) {
            String klassName = type.substring(1, type.length() - 1);
            klassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(klassName, false);
        }
        return heap.getObjectRef(new InstanceObject(type, count, klassIndex));
    }

    private long allocateArrayOfRef(String sourceKlassName, int cpIndex, int count) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String destKlassName = sourceKlass.getKlassNameByCPIndex((short) cpIndex);
        Klass destKlass = heap.getKlassLoader().getLoadedKlassByName(destKlassName);
        if (destKlass == null) {
            throw new RuntimeException("Class is not found");
        }
        int klassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(destKlassName, false);
        return heap.getObjectRef(allocateArrayOfRef(count, klassIndex));
    }

    private InstanceObject allocateArrayOfRef(int count, int klassIndex) {
        return new InstanceObject(JVMType.A.name(), count, klassIndex);
    }


}

