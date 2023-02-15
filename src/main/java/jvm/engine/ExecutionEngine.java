package jvm.engine;

import jvm.JVMType;
import jvm.Utils;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.lang.*;
import jvm.parser.Method;
import jvm.parser.Klass;
import jvm.parser.ConstantPoolEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.Socket;
import java.util.*;

import static jvm.engine.Opcode.*;
import static jvm.heap.InstanceFactory.getInstanceObject;
import static jvm.heap.KlassLoader.*;

public final class ExecutionEngine {

    public final static int NULL = 0;
    private final static String HASHCODE = "java/lang/Object.hashCode:()I";
    private final static String TO_STRING = "java/lang/Object.toString:()Ljava/lang/String;";
    private final static String STRING_PRINTLN = "java/io/PrintStream.println:(Ljava/lang/String;)V";
    private final static String STRING_PRINT = "java/io/PrintStream.print:(Ljava/lang/String;)V";
    private final static String CHAR_PRINTLN = "java/io/PrintStream.println:(C)V";
    private final static String CHAR_PRINT = "java/io/PrintStream.print:(C)V";
    private final static String INT_PRINTLN = "java/io/PrintStream.println:(I)V";
    private final static String INT_PRINT = "java/io/PrintStream.print:(I)V";
    private final static String INIT_SOCKET = "java/net/Socket.initSocket:(Ljava/lang/String;I)V";
    private final static String GET_INPUT_STREAM = "java/net/Socket.getInputStream:()Ljava/io/InputStream;";
    private final static String INPUT_STREAM = "java/io/InputStream";
    private final static String GET_OUTPUT_STREAM = "java/net/Socket.getOutputStream:()Ljava/io/OutputStream;";
    private final static String INIT_INPUT_STREAM_READER = "java/io/InputStreamReader.initInputStreamReader:(Ljava/io/InputStream;)V";
    private final static String INIT_BUFFERED_READER = "java/io/BufferedReader.initBufferedReader:(Ljava/io/Reader;)V";
    private final static String INIT_PRINT_WRITER = "java/io/PrintWriter.initPrintWriter:(Ljava/io/OutputStream;Z)V";
    private final static String PRINT_WRITER_PRINTLN = "java/io/PrintWriter.println:(Ljava/lang/String;)V";
    private final static String READ_LINE = "java/io/BufferedReader.readLine:()Ljava/lang/String;";
    private final static String RANDOM_NEXT_INT = "java/util/Random.nextInt:(I)I";
    private final static String STRING_INTERN = "java/lang/String.intern:()Ljava/lang/String;";

    private final Opcode[] table = new Opcode[256];
    @Nonnull
    private final Heap heap;
    @Nonnull
    private final StackFrame stack;
    @Nonnull
    private final Method[] stackMethod;
    int stackMethodPointer = 0;

    // VM Options
    private boolean exceptionDebugMode;

    private boolean symbolicRefResolution = true;
    byte[] byteCode;
    String klassName;
    int programCounter;

    private static final Map<Integer, Object> nativeObjects = new HashMap<>();

    public ExecutionEngine(@Nonnull Heap heap, @Nonnull StackFrame stackFrame) {
        this.heap = heap;
        this.stack = stackFrame;
        this.stackMethod = new Method[100];
        for (Opcode op : values()) {
            table[op.getOpcode()] = op;
        }
    }

    public long invoke(@Nonnull Method firstMethod) {

        programCounter = 0;
        stackMethod[0] = firstMethod;
        byteCode = firstMethod.getBytecode();
        klassName = firstMethod.getClassName();

        stack.init(firstMethod.getVarSize(), firstMethod.getOperandSize());
        while (true) {
            byte b = byteCode[programCounter++];
            Opcode op = table[b & 0xff];
            if (op == null) {
                throw new RuntimeException("Unrecognised opcode byte: "
                        + (b & 0xff)
                        + " encountered at position "
                        + (programCounter - 1)
                        + ". Stopping."
                        + "\n"
                        + "\n"
                        + getStackTrace(null, true));
            }
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
            try {
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
                            return getPureValue(checkValueType(stack.pop(), JVMType.A, op));
                        }
                        method = stackMethod[--stackMethodPointer];
                        stackMethod[stackMethodPointer + 1] = null;
                        byteCode = method.getBytecode();
                        klassName = method.getClassName();
                        stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), true);
                        programCounter = stack.programCounter;
                        break;
                    case ASTORE:
                        reference = checkValueType(stack.pop(), JVMType.A, op);
                        stack.setLocalVar(byteCode[programCounter++], reference);
                        break;
                    case ASTORE_0:
                        reference = checkValueType(stack.pop(), JVMType.A, op);
                        stack.setLocalVar(0, reference);
                        break;
                    case ASTORE_1:
                        reference = checkValueType(stack.pop(), JVMType.A, op);
                        stack.setLocalVar(1, reference);
                        break;
                    case ASTORE_2:
                        reference = checkValueType(stack.pop(), JVMType.A, op);
                        stack.setLocalVar(2, reference);
                        break;
                    case ASTORE_3:
                        reference = checkValueType(stack.pop(), JVMType.A, op);
                        stack.setLocalVar(3, reference);
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
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup)));
                        if (symbolicRefResolution) {
                            preserveDirectRefIndex(fieldValueIndex, GETFIELD_QUICK);
                        }
                        stack.push(object.getValue(fieldValueIndex));
                        break;
                    case GETFIELD_QUICK:
                        fieldValueIndex = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        stack.push(object.getValue(fieldValueIndex));
                        break;
                    case GETSTATIC:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                        fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));
                        preserveDirectRefIndexIfNeeded(objectRef, fieldValueIndex, GETSTATIC_QUICK);
                        stack.push(heap.getInstanceObject(objectRef).getValue(fieldValueIndex)); // to do deal with type to JVMValue
                        break;
                    case GETSTATIC_QUICK:
                        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        Method.DirectRef directRef = stackMethod[stackMethodPointer].getDirectRef(index);
                        stack.push(heap.getInstanceObject(directRef.getObjectRef()).getValue(directRef.getIndex()));
                        break;
                    case CHECKCAST:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        objectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, op));
                        if (objectRef == NULL) {
                            stack.push(setRefValueType(NULL));
                        } else {
                            object = heap.getInstanceObject(objectRef);
                            String castKlassName = heap.getKlassLoader().getLoadedKlassByName(klassName).getKlassNameByCPIndex((short) cpLookup);
                            if (object.isArray()) {
                                if (castKlassName.equals(object.getArrayType())) {
                                    stack.push(setRefValueType(objectRef));
                                } else {
                                    throw new ClassCastExceptionJVM(
                                            Objects.requireNonNull(object.getArrayType()).replace("/", ".")
                                                    + " cannot be cast to "
                                                    + castKlassName.replace("/", "."));
                                }
                            } else {
                                Klass klass = heap.getKlassLoader().getLoadedKlassByName(heap.getInstanceKlass(object.getKlassIndex()).getName());
                                if (checkCast(klass, castKlassName)) {
                                    stack.push(setRefValueType(objectRef));
                                } else {
                                    throw new ClassCastExceptionJVM(
                                            klass.getKlassName().replace("/", ".")
                                                    + " cannot be cast to "
                                                    + castKlassName.replace("/", "."));
                                }
                            }
                        }
                        break;
                    case GOTO:
                        programCounter += (byteCode[programCounter] << 8) + (byteCode[programCounter + 1] & 0xff) - 1;
                        break;
                    case IADD:
                        stack.push(setIntValueType(getPureValue(stack.pop()) + getPureValue(stack.pop())));
                        break;
                    case IAND:
                        stack.push(setIntValueType(getPureValue(stack.pop()) & getPureValue(stack.pop())));
                        break;
                    case INSTANCEOF:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        String className = heap.getKlassLoader().getLoadedKlassByName(klassName).getKlassNameByCPIndex((short) cpLookup);
                        objectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, op));
                        if (objectRef == NULL) {
                            stack.push(setIntValueType(0));
                            break;
                        }
                        object = heap.getInstanceObject(objectRef);
                        boolean instanceOf;
                        if (object.isArray()) {
                            instanceOf = className.equals(object.getArrayType());
                        } else {
                            Klass currentKlass = heap.getKlassLoader().getLoadedKlassByName(
                                    heap.getInstanceKlass(object.getKlassIndex()).getName());
                            instanceOf = checkCast(currentKlass, className);
                        }
                        stack.push(instanceOf ? setIntValueType(1) : setIntValueType(0));
                        break;
                    case ISHL:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(second << first));
                        break;
                    case ISHR:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(second >> first));
                        break;
                    case IUSHR:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(second >>> first));
                        break;
                    case IXOR:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(~second));
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
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first == 0) throw new ArithmeticException("cannot divide 0");
                        stack.push(setIntValueType(second / first));
                        break;
                    case IF_ACMPNE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.A, op))
                                != getPureValue(checkValueType(stack.pop(), JVMType.A, op))) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ACMPEQ:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.A, op))
                                == getPureValue(checkValueType(stack.pop(), JVMType.A, op))) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPEQ:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.I, op))
                                == getPureValue(checkValueType(stack.pop(), JVMType.I, op))) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPNE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.I, op))
                                != getPureValue(checkValueType(stack.pop(), JVMType.I, op))) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPLT:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (second < first) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPGT:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (second > first) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPGE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (second >= first) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IF_ICMPLE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (second <= first) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFEQ:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first == 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFGE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first >= 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFGT:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first > 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFLE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first <= 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFLT:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first < 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFNE:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        if (first != 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;

                    //---------------------------------------------------------------------------------------------------------------------------
                    case IFNONNULL:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.A, op)) != 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    case IFNULL:
                        jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        if (getPureValue(checkValueType(stack.pop(), JVMType.A, op)) == 0) {
                            programCounter += jumpTo - 3;
                        }
                        break;
                    //--------------------------------------------------------------------------------------------------------------------------------
                    case IINC:
                        /*
                         *The index is an unsigned byte that must be an index into the local variable array of the current frame.
                         * The const is an immediate signed byte. The local variable at index must contain an int.
                         * The value const is first sign-extended to an int, and then the local variable at index is incremented by that amount.
                         * */
                        index = byteCode[programCounter++];
                        first = getPureValue(checkValueType(stack.getLocalVar(index), JVMType.I, op));
                        stack.setLocalVar(index, setIntValueType(first + byteCode[programCounter++]));
                        break;
                    case I2C:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType((char) first));
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
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(first * second));
                        break;
                    case INEG:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(-first));
                        break;
                    //------------------------------------------------------------------------------------------------------------------------------------------
                    case INVOKESPECIAL:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        methodIndex = getMethodIndex(klassName, cpLookup); // todo restore to resolution
                        method = heap.getMethodRepo().getMethod(methodIndex);
                        handleMethod(method, op);
                        break;
                    case INVOKESTATIC:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
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
                        invokeVirtual(false, op);
                        break;
                    //------------------------------------------------------------------------------------------------------------------------------------------
                    case INVOKEVIRTUAL_QUICK:
                        invokeVirtual(true, op);
                        break;
                    case IOR:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(first | second));
                        break;
                    case IREM:
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(second % first));
                        break;
//                    ----------------------------------------------------------------------------------------------------------------------------------
                    case IRETURN: //return type boolean, byte, short, char, or int.
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
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        second = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        stack.push(setIntValueType(second - first));
                        break;

                    //--------------------------------------------------------------------------------------------------------------------------------------
                    case MONITORENTER:
                    case MONITOREXIT:
                        break;
                    //--------------------------------------------------------------------------------------------------------------------------------------
                    case NEW:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
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
                        stack.push(setRefValueType(allocateArray("[" + JVMType.values()[atype - 3].name(),
                                JVMType.values()[atype - 3].name(),
                                getPureValue(stack.pop()))));
                        break;
                    case ANEWARRAY:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        stack.push(setRefValueType(allocateArrayOfRef(
                                klassName,
                                cpLookup,
                                getPureValue(stack.pop()))));
                        break;
                    case MULTIANEWARRAY:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        int[] dimensions = new int[byteCode[programCounter++]];
                        for (int i = dimensions.length - 1; i >= 0; i--) {
                            dimensions[i] = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        }
                        String arrayType = getValueType(klassName, cpLookup);
                        object = createArrayOfRef(arrayType, dimensions[0], -1);
                        stack.push(setRefValueType(heap.getObjectRef(object)));
                        createMultiArray(1, dimensions, object, arrayType);
                        break;
                    case ARRAYLENGTH:
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        stack.push(setIntValueType(object.size()));
                        break;
                    case AALOAD:
                        index = getPureValue(stack.pop());
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        stack.push(checkValueType(object.getValue(index), JVMType.A, op));
                        break;
                    case IALOAD:
                        index = getPureValue(stack.pop());
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        stack.push(checkValueType(object.getValue(index), JVMType.I, op));
                        break;
                    case BALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type byte or of type boolean.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The byte value in the component of the array at index is retrieved, sign-extended to an int value, and pushed onto the top of the operand stack.
                         */
                        index = getPureValue(stack.pop());
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        stack.push(setIntValueType(getPureValue(checkByteOrBooleanValueType(object.getValue(index)))));
                        break;
                    case CALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The component of the array at index is retrieved and zero-extended to an int value. That value is pushed onto the operand stack.
                         */
                        index = getPureValue(stack.pop());
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        stack.push(setIntValueType(getPureValue(checkValueType(object.getValue(index), JVMType.C, op))));
                        break;
                    case AASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type reference.
                         * The index must be of type int and value must be of type reference. The arrayref, index, and value are popped from the operand stack.
                         * The reference value is stored as the component of the array at index.
                         */
                        value = checkValueType(stack.pop(), JVMType.A, op);
                        index = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        object.setValue(index, value);
                        break;
                    case IASTORE:
                        /*
                         * Store into int array
                         * Both index and value must be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is stored as the component of the array indexed by index.
                         */
                        value = checkValueType(stack.pop(), JVMType.I, op);
                        index = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        object.setValue(index, value);
                        break;
                    case BASTORE:
                        /*
                         * Store into byte or boolean array
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a byte and stored as the component of the array indexed by index.
                         */
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        index = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        JVMType type = object.getValueType();
                        if (type == JVMType.Z || type == JVMType.B) {
                            object.setValue(index, setValueType(first, type));
                        } else {
                            throw new RuntimeException("Wrong type of array\n" + getStackTrace(op, false));
                        }
                        break;
                    case CASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a char and stored as the component of the array indexed by index.
                         */
                        first = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        index = getPureValue(checkValueType(stack.pop(), JVMType.I, op));
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        checkArrayObject(object, stackMethod, stackMethodPointer, op);
                        object.setValue(index, setCharValueType(first));
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
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);

                        value = stack.pop();
                        object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, op)));
                        fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                        object.setValue(fieldValueIndex, value);
                        break;
                    case PUTSTATIC:
                        cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
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
                                createStringInstance(stack, entry.getStr(), true);
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
                        System.err.println(getStackTrace(op, true));
                        System.exit(1);
                }
            } catch (RuntimeExceptionJVM e) {
                if (exceptionDebugMode) {
                    if (e instanceof ClassCastExceptionJVM) {
                        throw new ClassCastExceptionJVM(e.getLocalizedMessage() + "\n" + getStackTrace(op, false));
                    } else if (e instanceof NullPointerExceptionJVM) {
                        throw new NullPointerExceptionJVM("\n" + getStackTrace(op, false));
                    } else if (e instanceof OutOfMemoryErrorJVM) {
                        throw new OutOfMemoryErrorJVM("\n" + getStackTrace(op, false));
                    } else if (e instanceof ClassNotFoundExceptionJVM) {
                        throw new ClassNotFoundExceptionJVM(e.getLocalizedMessage() + "\n" + getStackTrace(op, false));
                    } else {
                        throw e;
                    }
                } else {
                    System.out.println(Utils.changeJVMKlassNameToSystemKlassName(e.toString()) + "\n" + getStackTrace(op, false));
                    System.exit(-1);
                }
            } catch (Exception e) {
                throw new RuntimeException("\n" + getStackTrace(op, false) + "\n\n" + e);
            }
        }
    }

    @Nonnull
    private String getStackTrace(@Nullable Opcode opcode, boolean showMnemonics) {
        StringBuilder stackTrace = new StringBuilder();
        for (int i = stackMethodPointer; i >= 0; i--) {
            Method method = stackMethod[i];
            stackTrace.append("\tat ")
                    .append(method.getClassName())
                    .append(".")
                    .append(method.getNameAndType())
                    .append(i == stackMethodPointer && opcode != null ? " " + opcode : "")
                    .append("\n")
                    .append(showMnemonics ? method.getMnemonics() + "\n" : "");
        }
        return stackTrace.toString().replace("/", ".");
    }

    private boolean checkCast(@Nonnull Klass klass, @Nonnull String castKlassName) {
        String klassName = klass.getKlassName();
        if (JAVA_LANG_OBJECT.equals(klassName)) {
            return castKlassName.equals(klassName);
        }
        Klass parentKlass = heap.getKlassLoader().getLoadedKlassByName(klass.getParent());
        return castKlassName.equals(klassName) || checkCast(parentKlass, castKlassName);
    }

    private void invokeNativeMethod(@Nonnull StackFrame stack, @Nonnull Method method, @Nonnull Method[] stackMethod, int pointer, Opcode opcode) {
        String methodName = method.getClassName() + "." + method.getNameAndType();
        switch (methodName) {
            case HASHCODE: {
                InstanceObject object1 = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, opcode)));
                stack.push(setIntValueType(Objects.hashCode(object1)));
                break;
            }
            case TO_STRING: {
                int objectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                InstanceObject object1 = heap.getInstanceObject(objectRef);
                String result = heap.getInstanceKlass(object1.getKlassIndex()).getName().replace('/', '.')
                        + "@"
                        + Integer.toHexString(objectRef);
                createStringInstance(stack, result, false);
                break;
            }
            case STRING_PRINTLN: {
                int stringObjectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                System.out.println(getString(stringObjectRef, stackMethod, pointer, opcode));
                stack.pop();
                break;
            }
            case STRING_PRINT: {
                int stringObjectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                System.out.print(getString(stringObjectRef, stackMethod, pointer, opcode));
                stack.pop();
                break;
            }
            case CHAR_PRINT:
                System.out.print((char) getPureValue(checkValueType(stack.pop(), JVMType.I, opcode)));
                stack.pop();
                break;
            case CHAR_PRINTLN:
                System.out.println((char) getPureValue(checkValueType(stack.pop(), JVMType.I, opcode)));
                stack.pop();
                break;
            case INT_PRINT:
                System.out.print(getPureValue(checkValueType(stack.pop(), JVMType.I, opcode)));
                stack.pop();
                break;
            case INT_PRINTLN:
                System.out.println(getPureValue(checkValueType(stack.pop(), JVMType.I, opcode)));
                stack.pop();
                break;
            case INIT_SOCKET: {
                int socketObjRef = getPureValue(checkValueType(stack.getLocalVar(0), JVMType.A, opcode));
                int stringObjRef = getPureValue(checkValueType(stack.getLocalVar(1), JVMType.A, opcode));
                int port = getPureValue(checkValueType(stack.getLocalVar(2), JVMType.I, opcode));
                String ipAddress = getString(stringObjRef, stackMethod, pointer, opcode);
                try {
                    nativeObjects.put(socketObjRef, new Socket(ipAddress, port));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case GET_INPUT_STREAM:
            case GET_OUTPUT_STREAM: {
                int streamObjRef = allocateInstanceObjectAndGetReference(INPUT_STREAM);
                int socketObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                stack.push(setRefValueType(streamObjRef));
                Socket socket = (Socket) nativeObjects.get(socketObjRef);
                try {
                    nativeObjects.put(streamObjRef, GET_INPUT_STREAM.equals(methodName) ? socket.getInputStream() : socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case INIT_INPUT_STREAM_READER: {
                int inputStreamObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                int inputStreamReaderObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                InputStream inputStream = (InputStream) nativeObjects.get(inputStreamObjRef);
                nativeObjects.put(inputStreamReaderObjRef, new InputStreamReader(inputStream));
                break;
            }
            case INIT_BUFFERED_READER: {
                int inputStreamReaderObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                int bufferedReaderObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                Reader inputStreamReader = (Reader) nativeObjects.get(inputStreamReaderObjRef);
                nativeObjects.put(bufferedReaderObjRef, new BufferedReader(inputStreamReader));
                break;
            }
            case INIT_PRINT_WRITER: {
                int bool = getPureValue(checkValueType(stack.pop(), JVMType.I, opcode));
                int outputStreamObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                int printWriterObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                OutputStream outputStream = (OutputStream) nativeObjects.get(outputStreamObjRef);
                nativeObjects.put(printWriterObjRef, new PrintWriter(outputStream, bool == 1));
                break;
            }
            case PRINT_WRITER_PRINTLN: {
                int stringObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                String message = getString(stringObjRef, stackMethod, pointer, opcode);
                int printWriterObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                PrintWriter printWriter = (PrintWriter) nativeObjects.get(printWriterObjRef);
                printWriter.println(message);
                break;
            }
            case READ_LINE: {
                int bufferedReaderObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                BufferedReader bufferedReader = (BufferedReader) nativeObjects.get(bufferedReaderObjRef);
                try {
                    String message = bufferedReader.readLine();
                    createStringInstance(stack, message, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case RANDOM_NEXT_INT:
                int bound = getPureValue(checkValueType(stack.pop(), JVMType.I, opcode));
                int randomObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                stack.push(setIntValueType(new Random().nextInt(bound)));
                break;
            case STRING_INTERN:
                int stringObjRef = getPureValue(checkValueType(stack.pop(), JVMType.A, opcode));
                String str = getString(stringObjRef, stackMethod, pointer, opcode);
                createStringInstance(stack, str, true);
                break;
        }
    }

    @Nonnull
    private String getString(int objectRef, @Nonnull Method[] stackMethod, int pointer, Opcode opcode) {
        InstanceObject stringObject = heap.getInstanceObject(objectRef);
        InstanceObject charArrayObject = heap.getInstanceObject(getPureValue(checkValueType(
                stringObject.getValue(stringObject.getIndexByFieldName("value:[C")),
                JVMType.A,
                opcode)));
        char[] buf = new char[charArrayObject.size()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) charArrayObject.getValue(i);
        }
        return String.valueOf(buf);
    }

    private void createMultiArray(int indexDim, int[] dimensions, InstanceObject object, String type) {
        String arrayType = type.substring(type.indexOf('[') + 1);
        for (int i = 0; i < object.size(); i++) {
            if (indexDim == dimensions.length - 1) {
                object.setValue(i, setRefValueType(allocateArray(arrayType, arrayType.substring(1), dimensions[indexDim])));
            } else {
                InstanceObject newObject = createArrayOfRef(arrayType, dimensions[indexDim], -1);
                object.setValue(i, setRefValueType(heap.getObjectRef(newObject)));
                createMultiArray(indexDim + 1, dimensions, newObject, arrayType);
            }
        }
    }

    private long checkValueType(long value, @Nonnull JVMType type, Opcode opcode) {
        if (type.equals(JVMType.I) || type.equals(JVMType.Z)) {
            if (getValueType(value) == JVMType.I.ordinal() || getValueType(value) == JVMType.Z.ordinal()) {
                return value;
            }
        }
        if (getValueType(value) != type.ordinal()) {
            throw new RuntimeException("Wrong types: "
                    + JVMType.values()[getValueType(value)]
                    + " is not equal "
                    + type.name()
                    + "\n"
                    + getStackTrace(opcode, false));
        }
        return value;
    }

    private long checkByteOrBooleanValueType(long value) {
        if (getValueType(value) == JVMType.Z.ordinal() || getValueType(value) == JVMType.B.ordinal()) {
            return value;
        }
        throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(value)] + " is not equal " + "byte or boolean");
    }

    private void checkArrayObject(@Nonnull InstanceObject object, @Nonnull Method[] stackMethod, int pointer, @Nullable Opcode opcode) {
        if (!object.isArray()) {
            throw new RuntimeException("Object is not array\n" + getStackTrace(opcode, false));
        }
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    private String getValueType(String klassName, int cpLookup) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(klassName);
        return sourceKlass.getKlassNameByCPIndex((short) cpLookup);
    }

    private long setIntValueType(int value) {
        return setValueType(JVMType.I.ordinal()) ^ value;
    }

    private long setCharValueType(int value) {
        return setValueType(JVMType.C.ordinal()) ^ value;
    }

    private long setValueType(int value, @Nonnull JVMType type) {
        return setValueType(type.ordinal()) ^ value;
    }

    private long setRefValueType(int value) {
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

    private int getInstanceKlassIndex(@Nonnull String fullName) {
        String klassName = getKlassName(fullName);
        return indexNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true), klassName);
    }

    private int getInstanceKlassIndexByKlassName(@Nonnull String klassName) {
        return indexNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true), klassName);
    }

    private int indexNonNull(@Nullable Integer index, @Nonnull String name) {
        if (index == null) {
            throw new ClassNotFoundExceptionJVM("Class " + name + " is not found");
        }
        return index;
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
        Klass cpKlass = heap.getInstanceKlass(getInstanceKlassIndex(fullName)).getCpKlass();
        Method method = null;
        String parentName = null;
        while (method == null) {
            method = cpKlass.getMethodByName(getMethodName(fullName));
            parentName = cpKlass.getParent();
            if (ABSENCE.equals(parentName)) {
                break;
            }
            cpKlass = heap.getInstanceKlass(getInstanceKlassIndexByKlassName(cpKlass.getParent())).getCpKlass();
        }
        if (method == null) {
            throw new VirtualMachineErrorJVM("method " + getMethodName(fullName) + " is not found");
        }
        return method.getArgSize();
    }

    private void createStringInstance(@Nonnull StackFrame stack, @Nonnull String str, boolean toPoolOfStrings) {
        Integer objRef;
        if (heap.isEnabledCacheString()) {
            objRef = heap.getStringRefFromPool(str);
            if (objRef != null) {
                stack.push(setRefValueType(objRef));
                return;
            }
        }
        /*----------------------------------*/
        // before second allocation, we have to push the first object reference on the stack,
        // or that first object can be cleared by GC
        InstanceObject stringObj = allocateInstanceObject(STRING);
        objRef = getInstanceObjectReference(stringObj);
        stack.push(setRefValueType(objRef));
        int charArrayRef = allocateArray("[" + JVMType.C.name(), JVMType.C.name(), str.length());
        /*----------------------------------*/
        InstanceObject charArrayObj = heap.getInstanceObject(charArrayRef);
        for (int i = 0; i < str.length(); i++) {
            charArrayObj.setValue(i, setCharValueType(str.charAt(i)));
        }
        stringObj.setValue(stringObj.getIndexByFieldName("value:[C"), setRefValueType(charArrayRef));

        if (heap.isEnabledCacheString() && toPoolOfStrings) {
            heap.putStringRefToPool(str, objRef, charArrayRef);
        }
    }

    private int allocateInstanceObjectAndGetReference(String sourceKlassName, int cpIndex) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String destKlassName = sourceKlass.getKlassNameByCPIndex((short) cpIndex);
        return allocateInstanceObjectAndGetReference(destKlassName);
    }

    private int allocateInstanceObjectAndGetReference(@Nonnull String klassName) {
        return getInstanceObjectReference(allocateInstanceObject(klassName));
    }

    private int getInstanceObjectReference(@Nonnull InstanceObject object) {
        return heap.getObjectRef(object);
    }

    @Nonnull
    private InstanceObject allocateInstanceObject(@Nonnull String klassName) {
        Klass destKlass = heap.getKlassLoader().getLoadedKlassByName(klassName);
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

        return getInstanceObject(heap, fields, indexNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true), klassName));
    }


    private int allocateArray(@Nonnull String arrayType, @Nonnull String valueType, int count) {
        int klassIndex = -1;
        if (arrayType.startsWith("[") && arrayType.endsWith(";")) {
            String klassName = arrayType.substring(arrayType.indexOf('L') + 1, arrayType.length() - 1);
            klassIndex = indexNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true), klassName);
        }
        return heap.getObjectRef(getInstanceObject(heap, arrayType, valueType, count, klassIndex));
    }

    private int allocateArrayOfRef(String sourceKlassName, int cpIndex, int count) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String destKlassName = sourceKlass.getKlassNameByCPIndex((short) cpIndex);
        Klass destKlass = heap.getKlassLoader().getLoadedKlassByName(destKlassName);
        if (destKlass == null) {
            throw new RuntimeException("Class is not found");
        }
        int klassIndex = indexNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(destKlassName, true), destKlassName);
        return heap.getObjectRef(createArrayOfRef("[L" + destKlassName + ";", count, klassIndex));
    }

    private InstanceObject createArrayOfRef(String arrayType, int count, int klassIndex) {
        return getInstanceObject(heap, arrayType, JVMType.A.name(), count, klassIndex);
    }

    public void setExceptionDebugMode(boolean exceptionDebugMode) {
        this.exceptionDebugMode = exceptionDebugMode;
    }

    private void preserveDirectRefIndex(int index, Opcode opcode) {
        int counter = programCounter - 3;
        byteCode[counter++] = opcode.b();
        byteCode[counter++] = (byte) (index >> 8);
        byteCode[counter] = (byte) index;
    }

    private void invokeVirtual(boolean quick, @Nonnull Opcode op) {
        int index;
        int argSize;
        if (quick) {
            int directRefIndex = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
            Method.DirectRef directRef = stackMethod[stackMethodPointer].getDirectRef(directRefIndex);
            index = directRef.getIndex();
            argSize = directRef.getObjectRef();
        } else {
            index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
            argSize = getArgSize(klassName, index);
        }
        long reference = stack.getObjectRefBeforeInvoke(argSize);
        int objectRef = getPureValue(checkValueType(reference, JVMType.A, op));
        int klassIndex = heap.getInstanceObject(checkNotNull(objectRef)).getKlassIndex();
        int virtualMethodIndex;
        if (quick) {
            virtualMethodIndex = index;
        } else {
            virtualMethodIndex = getVirtualMethodIndex(klassName, index, klassIndex);
            preserveDirectRefIndexIfNeeded(argSize, virtualMethodIndex, INVOKEVIRTUAL_QUICK);
        }
        int methodIndex = heap.getInstanceKlass(klassIndex).getMethodIndex(virtualMethodIndex);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        handleMethod(method, op);
    }

    private void preserveDirectRefIndexIfNeeded(int objectRef, int index, @Nonnull Opcode opcode) {
        if (symbolicRefResolution) {
            int directRefIndex = stackMethod[stackMethodPointer].addDirectRef(objectRef, index);
            preserveDirectRefIndex(directRefIndex, opcode);
        }
    }

    private void handleMethod(@Nonnull Method method, @Nonnull Opcode op) {
        if (!method.isNative()) {
            initNewMethod(method);
        } else {
            invokeNativeMethod(stack, method, stackMethod, stackMethodPointer, op);
        }
    }

    private void initNewMethod(@Nonnull Method method) {
        byteCode = method.getBytecode();
        klassName = method.getClassName();
        stackMethod[++stackMethodPointer] = method;
        stack.programCounter = programCounter;
        programCounter = 0;
        stack.initNewMethodStack(method.getArgSize() + 1, method.getVarSize(), method.getOperandSize());
    }

    private int checkNotNull(int objectRef) {
        if (objectRef == NULL) {
            throw new NullPointerExceptionJVM();
        }
        return objectRef;
    }

}

