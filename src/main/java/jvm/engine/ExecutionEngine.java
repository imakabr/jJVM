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
import java.util.function.*;

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
    private byte[] byteCode;
    private String klassName;
    private int programCounter;

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

            InstanceObject object;
            int first;
            int cpIndex;
            try {
                switch (op) {
                    case ACONST_NULL:
                        // push the null object reference onto the operand stack
                        pushRefValueOntoStack(NULL);
                        break;
                    case ALOAD:
                        // The objectref in the local variable at index is pushed onto the operand stack
                        pushOntoStackFromLocalVar(byteCode[programCounter++]);
                        break;
                    case ALOAD_0:
                        pushOntoStackFromLocalVar(0);
                        break;
                    case ALOAD_1:
                        pushOntoStackFromLocalVar(1);
                        break;
                    case ALOAD_2:
                        pushOntoStackFromLocalVar(2);
                        break;
                    case ALOAD_3:
                        pushOntoStackFromLocalVar(3);
                        break;
                    case ILOAD:
                        //Load int from local variable to the operand stack
                        stack.push(stack.getLocalVar(byteCode[programCounter++]));
                        break;
                    case ILOAD_0:
                        pushIntValueOntoStackFromLocalVar(0, op);
                        break;
                    case ILOAD_1:
                        pushIntValueOntoStackFromLocalVar(1, op);
                        break;
                    case ILOAD_2:
                        pushIntValueOntoStackFromLocalVar(2, op);
                        break;
                    case ILOAD_3:
                        pushIntValueOntoStackFromLocalVar(3, op);
                        break;
                    case ASTORE:
                        setLocalVarFromStack(byteCode[programCounter++], op);
                        break;
                    case ASTORE_0:
                        setLocalVarFromStack(0, op);
                        break;
                    case ASTORE_1:
                        setLocalVarFromStack(1, op);
                        break;
                    case ASTORE_2:
                        setLocalVarFromStack(2, op);
                        break;
                    case ASTORE_3:
                        setLocalVarFromStack(3, op);
                        break;
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
                    case ICONST_0:
                        pushIntValueOntoStack(0);
                        break;
                    case ICONST_1:
                        pushIntValueOntoStack(1);
                        break;
                    case ICONST_2:
                        pushIntValueOntoStack(2);
                        break;
                    case ICONST_3:
                        pushIntValueOntoStack(3);
                        break;
                    case ICONST_4:
                        pushIntValueOntoStack(4);
                        break;
                    case ICONST_5:
                        pushIntValueOntoStack(5);
                        break;
                    case ICONST_M1:
                        pushIntValueOntoStack(-1);
                        break;
                    case BIPUSH:
                        //The immediate byte is sign-extended to an int value. That value is pushed onto the operand stack.
                        pushIntValueOntoStack(byteCode[programCounter++]);
                        break;
                    case DUP:
                        //Duplicate the top operand stack value
                        stack.dup();
                        break;
                    case DUP_X1:
                        stack.dupX1();
                        break;
                    case GETFIELD:
                        pushFieldOntoStackFromInstanceObject(false, op);
                        break;
                    case GETFIELD_QUICK:
                        pushFieldOntoStackFromInstanceObject(true, op);
                        break;
                    case GETSTATIC:
                        pushStaticFieldOntoStackFromInstanceObject(false);
                        break;
                    case GETSTATIC_QUICK:
                        pushStaticFieldOntoStackFromInstanceObject(true);
                        break;
                    case PUTFIELD:
                        putFieldToInstanceObjectFromStack(false, op);
                        break;
                    case PUTFIELD_QUICK:
                        putFieldToInstanceObjectFromStack(true, op);
                        break;
                    case PUTSTATIC:
                        putStaticFieldToInstanceObjectFromStack(false);
                        break;
                    case PUTSTATIC_QUICK:
                        putStaticFieldToInstanceObjectFromStack(true);
                        break;
                    case CHECKCAST:
                        checkCast(op);
                        break;
                    case GOTO:
                        programCounter += (byteCode[programCounter] << 8) + (byteCode[programCounter + 1] & 0xff) - 1;
                        break;
                    case INSTANCEOF:
                        checkInstanceOf(op);
                        break;
                    case IADD:
                        evaluateIntValueAndPushBackOntoStack(Integer::sum, op);
                        break;
                    case IAND:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal & firstVal, op);
                        break;
                    case ISHL:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal << firstVal, op);
                        break;
                    case ISHR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal >> firstVal, op);
                        break;
                    case IUSHR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal >>> firstVal, op);
                        break;
                    case IXOR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> ~secondVal, op);
                        break;
                    case IDIV:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> {
                            if (firstVal == 0) {
                                throw new ArithmeticException("cannot divide 0");
                            }
                            return secondVal / firstVal;
                        }, op);
                        break;
                    case IMUL:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> firstVal * secondVal, op);
                        break;
                    case ISUB:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal - firstVal, op);
                        break;
                    case IOR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> firstVal | secondVal, op);
                        break;
                    case IREM:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal % firstVal, op);
                        break;
                    case IF_ACMPNE:
                        compareValuesFromStack((firstVal, secondVal) -> !Objects.equals(secondVal, firstVal), JVMType.A, op);
                        break;
                    case IF_ACMPEQ:
                        compareValuesFromStack((firstVal, secondVal) -> Objects.equals(secondVal, firstVal), JVMType.A, op);
                        break;
                    case IF_ICMPEQ:
                        compareValuesFromStack((firstVal, secondVal) -> Objects.equals(secondVal, firstVal), JVMType.I, op);
                        break;
                    case IF_ICMPNE:
                        compareValuesFromStack((firstVal, secondVal) -> !Objects.equals(secondVal, firstVal), JVMType.I, op);
                        break;
                    case IF_ICMPLT:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal < firstVal, JVMType.I, op);
                        break;
                    case IF_ICMPGT:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal > firstVal, JVMType.I, op);
                        break;
                    case IF_ICMPGE:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal >= firstVal, JVMType.I, op);
                        break;
                    case IF_ICMPLE:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal <= firstVal, JVMType.I, op);
                        break;
                    case IFEQ:
                        compareValuesFromStack(val -> val == 0, JVMType.I, op);
                        break;
                    case IFGE:
                        compareValuesFromStack(val -> val >= 0, JVMType.I, op);
                        break;
                    case IFGT:
                        compareValuesFromStack(val -> val > 0, JVMType.I, op);
                        break;
                    case IFLE:
                        compareValuesFromStack(val -> val <= 0, JVMType.I, op);
                        break;
                    case IFLT:
                        compareValuesFromStack(val -> val < 0, JVMType.I, op);
                        break;
                    case IFNE:
                        compareValuesFromStack(val -> val != 0, JVMType.I, op);
                        break;
                    case IFNONNULL:
                        compareValuesFromStack(val -> val != 0, JVMType.A, op);
                        break;
                    case IFNULL:
                        compareValuesFromStack(val -> val == 0, JVMType.A, op);
                        break;
                    case IINC:
                        /*
                         *The index is an unsigned byte that must be an index into the local variable array of the current frame.
                         * The const is an immediate signed byte. The local variable at index must contain an int.
                         * The value const is first sign-extended to an int, and then the local variable at index is incremented by that amount.
                         * */
                        int index = byteCode[programCounter++];
                        first = getIntValue(stack.getLocalVar(index), op);
                        stack.setLocalVar(index, setIntValueType(first + byteCode[programCounter++]));
                        break;
                    case I2C:
                        first = getIntValue(stack.pop(), op);
                        pushIntValueOntoStack(first);
                        break;
                    case INEG:
                        first = getIntValue(stack.pop(), op);
                        pushIntValueOntoStack(-first);
                        break;
                    case INVOKESPECIAL:
                        invokeNonVirtualMethod(false, false, op);
                        break;
                    case INVOKESPECIAL_QUICK:
                        invokeNonVirtualMethod(true, false, op);
                        break;
                    case INVOKESTATIC:
                        invokeNonVirtualMethod(false, true, op);
                        break;
                    case INVOKESTATIC_QUICK:
                        invokeNonVirtualMethod(true, true, op);
                        break;
                    case INVOKEVIRTUAL:
                        invokeVirtualMethod(false, op);
                        break;
                    case INVOKEVIRTUAL_QUICK:
                        invokeVirtualMethod(true, op);
                        break;
                    case ARETURN:
                        if (stack.invokeCount == 0) {
                            return getRefValue(stack.pop(), op);
                        }
                        destroyCurrentMethod(true);
                        break;
                    case IRETURN:
                        //return type boolean, byte, short, char, or int.
                        if (stack.invokeCount == 0) {
                            return getPureValue(stack.pop());
                        }
                        destroyCurrentMethod(true);
                        break;
                    case RETURN:
                        if (stackMethodPointer == 0) {
                            return 0;
                        }
                        destroyCurrentMethod(false);
                        break;
                    case NEW:
                        cpIndex = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        pushRefValueOntoStack(allocateInstanceObjectAndGetReference(cpIndex));
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
                        pushRefValueOntoStack(allocateArray("[" + JVMType.values()[atype - 3].name(),
                                JVMType.values()[atype - 3].name(),
                                getPureValue(stack.pop())));
                        break;
                    case ANEWARRAY:
                        cpIndex = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        pushRefValueOntoStack(allocateArrayOfRef(cpIndex, getPureValue(stack.pop())));
                        break;
                    case MULTIANEWARRAY:
                        cpIndex = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                        int[] dimensions = new int[byteCode[programCounter++]];
                        for (int i = dimensions.length - 1; i >= 0; i--) {
                            dimensions[i] = getIntValue(stack.pop(), op);
                        }
                        String arrayType = getKlassName(cpIndex);
                        object = createArrayOfRef(arrayType, dimensions[0], -1);
                        pushRefValueOntoStack(getInstanceObjectReference(object));
                        createMultiArray(1, dimensions, object, arrayType);
                        break;
                    case ARRAYLENGTH:
                        object = getInstanceObjectByValue(stack.pop(), op);
                        checkArrayObject(object, op);
                        pushIntValueOntoStack(object.size());
                        break;
                    case AALOAD:
                        pushOntoStackFromArray(val -> checkValueType(val, JVMType.A, op), op);
                        break;
                    case IALOAD:
                        pushOntoStackFromArray(val -> checkValueType(val, JVMType.I, op), op);
                        break;
                    case BALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type byte or of type boolean.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The byte value in the component of the array at index is retrieved, sign-extended to an int value, and pushed onto the top of the operand stack.
                         */
                        pushOntoStackFromArray(val -> setIntValueType(getPureValue(checkByteOrBooleanValueType(val))), op);
                        break;
                    case CALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The component of the array at index is retrieved and zero-extended to an int value. That value is pushed onto the operand stack.
                         */
                        pushOntoStackFromArray(val -> setIntValueType(getPureValue(checkValueType(val, JVMType.C, op))), op);
                        break;
                    case AASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type reference.
                         * The index must be of type int and value must be of type reference. The arrayref, index, and value are popped from the operand stack.
                         * The reference value is stored as the component of the array at index.
                         */
                        storeToArrayFromStack((val, obj) -> val, JVMType.A, op);
                        break;
                    case IASTORE:
                        /*
                         * Store into int array
                         * Both index and value must be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> val, JVMType.I, op);
                        break;
                    case BASTORE:
                        /*
                         * Store into byte or boolean array
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a byte and stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> {
                            JVMType type = obj.getValueType();
                            if (type == JVMType.Z || type == JVMType.B) {
                                return setValueType(getPureValue(val), type);
                            } else {
                                throw new RuntimeException("Wrong type of array\n" + getStackTrace(op, false));
                            }
                        }, JVMType.I, op);
                        break;
                    case CASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a char and stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> setCharValueType(getPureValue(val)), JVMType.I, op);
                        break;
                    case POP:
                        stack.pop();
                        break;
                    case RET:
                        throw new IllegalArgumentException("Illegal opcode byte: " + (b & 0xff) + " encountered at position " + (programCounter - 1) + ". Stopping.");
                    case SIPUSH:
                        pushIntValueOntoStack(((int) (byteCode[programCounter++]) << 8) + (byteCode[programCounter++] & 0xff));
                        break;
                    case SWAP:
                        long firstVal = stack.pop();
                        long secondVal = stack.pop();
                        stack.push(firstVal);
                        stack.push(secondVal);
                        break;
                    case LDC:
                        cpIndex = byteCode[programCounter++];
                        ConstantPoolEntry entry = getSourceKlass().getCPItem(cpIndex - 1);
                        switch (entry.getType()) {
                            case INTEGER:
                                pushIntValueOntoStack((Integer) entry.getNum());
                                break;
                            case STRING:
                                createStringInstance(entry.getStr(), true);
                                break;
                        }
                        break;
                    case NOP:
                    case POP2:
                    case MONITORENTER:
                    case MONITOREXIT:
                        break;
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
        Klass parentKlass = getKlass(klass.getParent());
        return castKlassName.equals(klassName) || checkCast(parentKlass, castKlassName);
    }

    private void invokeNativeMethod(@Nonnull Method method, Opcode opcode) {
        String methodName = method.getClassName() + "." + method.getNameAndType();
        switch (methodName) {
            case HASHCODE: {
                InstanceObject object1 = getInstanceObjectByValue(stack.pop(), opcode);
                pushIntValueOntoStack(Objects.hashCode(object1));
                break;
            }
            case TO_STRING: {
                int objectRef = getRefValue(stack.pop(), opcode);
                InstanceObject object1 = getInstanceObjectByRef(objectRef);
                String result = getNameFromInstanceKlassByIndex(object1.getKlassIndex()).replace('/', '.')
                        + "@"
                        + Integer.toHexString(objectRef);
                createStringInstance(result, false);
                break;
            }
            case STRING_PRINTLN: {
                int stringObjectRef = getRefValue(stack.pop(), opcode);
                System.out.println(getString(stringObjectRef, opcode));
                stack.pop();
                break;
            }
            case STRING_PRINT: {
                int stringObjectRef = getRefValue(stack.pop(), opcode);
                System.out.print(getString(stringObjectRef, opcode));
                stack.pop();
                break;
            }
            case CHAR_PRINT:
                System.out.print((char) getIntValue(stack.pop(), opcode));
                stack.pop();
                break;
            case CHAR_PRINTLN:
                System.out.println((char) getIntValue(stack.pop(), opcode));
                stack.pop();
                break;
            case INT_PRINT:
                System.out.print(getIntValue(stack.pop(), opcode));
                stack.pop();
                break;
            case INT_PRINTLN:
                System.out.println(getIntValue(stack.pop(), opcode));
                stack.pop();
                break;
            case INIT_SOCKET: {
                int socketObjRef = getRefValue(stack.getLocalVar(0), opcode);
                int stringObjRef = getRefValue(stack.getLocalVar(1), opcode);
                int port = getIntValue(stack.getLocalVar(2), opcode);
                String ipAddress = getString(stringObjRef, opcode);
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
                int socketObjRef = getRefValue(stack.pop(), opcode);
                pushRefValueOntoStack(streamObjRef);
                Socket socket = (Socket) nativeObjects.get(socketObjRef);
                try {
                    nativeObjects.put(streamObjRef, GET_INPUT_STREAM.equals(methodName) ? socket.getInputStream() : socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case INIT_INPUT_STREAM_READER: {
                int inputStreamObjRef = getRefValue(stack.pop(), opcode);
                int inputStreamReaderObjRef = getRefValue(stack.pop(), opcode);
                InputStream inputStream = (InputStream) nativeObjects.get(inputStreamObjRef);
                nativeObjects.put(inputStreamReaderObjRef, new InputStreamReader(inputStream));
                break;
            }
            case INIT_BUFFERED_READER: {
                int inputStreamReaderObjRef = getRefValue(stack.pop(), opcode);
                int bufferedReaderObjRef = getRefValue(stack.pop(), opcode);
                Reader inputStreamReader = (Reader) nativeObjects.get(inputStreamReaderObjRef);
                nativeObjects.put(bufferedReaderObjRef, new BufferedReader(inputStreamReader));
                break;
            }
            case INIT_PRINT_WRITER: {
                int bool = getIntValue(stack.pop(), opcode);
                int outputStreamObjRef = getRefValue(stack.pop(), opcode);
                int printWriterObjRef = getRefValue(stack.pop(), opcode);
                OutputStream outputStream = (OutputStream) nativeObjects.get(outputStreamObjRef);
                nativeObjects.put(printWriterObjRef, new PrintWriter(outputStream, bool == 1));
                break;
            }
            case PRINT_WRITER_PRINTLN: {
                int stringObjRef = getRefValue(stack.pop(), opcode);
                String message = getString(stringObjRef, opcode);
                int printWriterObjRef = getRefValue(stack.pop(), opcode);
                PrintWriter printWriter = (PrintWriter) nativeObjects.get(printWriterObjRef);
                printWriter.println(message);
                break;
            }
            case READ_LINE: {
                int bufferedReaderObjRef = getRefValue(stack.pop(), opcode);
                BufferedReader bufferedReader = (BufferedReader) nativeObjects.get(bufferedReaderObjRef);
                try {
                    String message = bufferedReader.readLine();
                    createStringInstance(message, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case RANDOM_NEXT_INT:
                int bound = getIntValue(stack.pop(), opcode);
                pushIntValueOntoStack(new Random().nextInt(bound));
                break;
            case STRING_INTERN:
                int stringObjRef = getRefValue(stack.pop(), opcode);
                String str = getString(stringObjRef, opcode);
                createStringInstance(str, true);
                break;
        }
    }

    @Nonnull
    private String getString(int objectRef, Opcode opcode) {
        InstanceObject stringObject = getInstanceObjectByRef(objectRef);
        InstanceObject charArrayObject = getInstanceObjectByValue(
                stringObject.getValue(stringObject.getIndexByFieldName("value:[C")), opcode);
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
                object.setValue(i, setRefValueType(getInstanceObjectReference(newObject)));
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

    private void checkArrayObject(@Nonnull InstanceObject object, @Nullable Opcode opcode) {
        if (!object.isArray()) {
            throw new RuntimeException("Object is not array\n" + getStackTrace(opcode, false));
        }
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    @Nonnull
    private String getKlassName(int cpIndex) {
        return getSourceKlass().getKlassNameByCPIndex((short) cpIndex);
    }

    private long setIntValueType(int value) {
        return setValueType(JVMType.I.ordinal()) ^ value;
    }

    private void pushIntValueOntoStack(int value) {
        stack.push(setIntValueType(value));
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

    private void pushRefValueOntoStack(int value) {
        stack.push(setRefValueType(value));
    }

    private void pushOntoStackFromLocalVar(int index) {
        long reference = stack.getLocalVar(index);
        stack.push(reference);
    }

    private void pushIntValueOntoStackFromLocalVar(int index, @Nonnull Opcode opcode) {
        int value = getIntValue(stack.getLocalVar(index), opcode);
        pushIntValueOntoStack(value);
    }

    private void setLocalVarFromStack(int index, @Nonnull Opcode opcode) {
        long reference = checkValueType(stack.pop(), JVMType.A, opcode);
        stack.setLocalVar(index, reference);
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getPureValue(long value) {
        return (int) value;
    }

    private int getIntValue(long value, @Nonnull Opcode opcode) {
        return getCheckedValue(value, JVMType.I, opcode);
    }

    private int getRefValue(long value, @Nonnull Opcode opcode) {
        return getCheckedValue(value, JVMType.A, opcode);
    }

    private int getCheckedValue(long value, JVMType type, @Nonnull Opcode opcode) {
        return getPureValue(checkValueType(value, type, opcode));
    }

    private String getName(@Nonnull String fullName) {
        return fullName.substring(fullName.indexOf(".") + 1);
    }

    private String getFieldName(@Nonnull String klassFieldName) {
        return getName(klassFieldName);
    }

    private String getMethodName(@Nonnull String klassMethodName) {
        return getName(klassMethodName);
    }

    private String getKlassName(@Nonnull String fullName) {
        return fullName.substring(0, fullName.indexOf("."));
    }

    @Nonnull
    private InstanceKlass getInstanceKlassByName(@Nonnull String fullName) {
        return getInstanceKlassByIndex(getInstanceKlassIndexByKlassName(fullName));
    }

    @Nonnull
    private String getNameFromInstanceKlassByIndex(int instKlassIndex) {
        return getInstanceKlassByIndex(instKlassIndex).getName();
    }

    @Nonnull
    private InstanceKlass getInstanceKlassByIndex(int instKlassIndex) {
        return heap.getInstanceKlass(instKlassIndex);
    }

    @Nonnull
    private InstanceObject getInstanceObjectByValue(long value, @Nonnull Opcode opcode) {
        return getInstanceObjectByRef(getRefValue(value, opcode));
    }
    @Nonnull
    private InstanceObject getInstanceObjectByRef(int objectRef) {
        return heap.getInstanceObject(checkNotNull(objectRef));
    }

    private int getInstanceKlassIndexByKlassName(@Nonnull String klassName) {
        Integer index = heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true);
        if (index == null) {
            throw new ClassNotFoundExceptionJVM("Class " + klassName + " is not found");
        }
        return index;
    }

    @Nonnull
    private String getKlassFieldName(int cpLookup) {
        return getSourceKlass().getFieldByCPIndex((short) cpLookup);
    }

    private int getMethodIndex(int cpIndex) {
        return heap.getMethodRepo().getIndexByName(getKlassMethodName(cpIndex));
    }

    private int getStaticMethodIndex(int cpIndex) {
        String klassMethodName = getKlassMethodName(cpIndex);
        InstanceKlass instanceKlass = getInstanceKlassByName(getKlassName(klassMethodName));
        return instanceKlass.getIndexByMethodName(getMethodName(klassMethodName));
    }

    private int getVirtualMethodIndex(int cpIndex, int klassIndex) {
        InstanceKlass instanceKlass = getInstanceKlassByIndex(klassIndex);
        return instanceKlass.getIndexByVirtualMethodName(getMethodName(getKlassMethodName(cpIndex)));
    }

    @Nonnull
    private String getKlassMethodName(int cpIndex) {
        return getSourceKlass().getMethodNameByCPIndex((short) cpIndex);
    }

    @Nonnull
    private Klass getSourceKlass() {
        return getKlass(klassName);
    }

    @Nonnull
    private Klass getKlass(@Nonnull String klassName) {
        return heap.getKlassLoader().getLoadedKlassByName(klassName);
    }

    private int getArgSize(int cpIndex) {
        String klassMethodName = getKlassMethodName(cpIndex);
        Klass cpKlass = getInstanceKlassByName(getKlassName(klassMethodName)).getCpKlass();
        Method method = null;
        String parentName;
        while (method == null) {
            method = cpKlass.getMethodByName(getMethodName(klassMethodName));
            parentName = cpKlass.getParent();
            if (ABSENCE.equals(parentName)) {
                break;
            }
            cpKlass = getInstanceKlassByName(cpKlass.getParent()).getCpKlass();
        }
        if (method == null) {
            throw new VirtualMachineErrorJVM("method " + getMethodName(klassMethodName) + " is not found");
        }
        return method.getArgSize();
    }

    private void createStringInstance(@Nonnull String str, boolean toPoolOfStrings) {
        Integer objRef;
        if (heap.isEnabledCacheString()) {
            objRef = heap.getStringRefFromPool(str);
            if (objRef != null) {
                pushRefValueOntoStack(objRef);
                return;
            }
        }
        /*----------------------------------*/
        // before second allocation, we have to push the first object reference on the stack,
        // or that first object can be cleared by GC
        InstanceObject stringObj = allocateInstanceObject(STRING);
        objRef = getInstanceObjectReference(stringObj);
        pushRefValueOntoStack(objRef);
        int charArrayRef = allocateArray("[" + JVMType.C.name(), JVMType.C.name(), str.length());
        /*----------------------------------*/
        InstanceObject charArrayObj = getInstanceObjectByRef(charArrayRef);
        for (int i = 0; i < str.length(); i++) {
            charArrayObj.setValue(i, setCharValueType(str.charAt(i)));
        }
        stringObj.setValue(stringObj.getIndexByFieldName("value:[C"), setRefValueType(charArrayRef));

        if (heap.isEnabledCacheString() && toPoolOfStrings) {
            heap.putStringRefToPool(str, objRef, charArrayRef);
        }
    }

    private int allocateInstanceObjectAndGetReference(int cpIndex) {
        return allocateInstanceObjectAndGetReference(getKlassName(cpIndex));
    }

    private int allocateInstanceObjectAndGetReference(@Nonnull String klassName) {
        return getInstanceObjectReference(allocateInstanceObject(klassName));
    }

    private int getInstanceObjectReference(@Nonnull InstanceObject object) {
        return heap.getObjectRef(object);
    }

    @Nonnull
    private InstanceObject allocateInstanceObject(@Nonnull String klassName) {
        Klass destKlass = getKlass(klassName);
        List<Klass> klasses = new ArrayList<>();
        Klass current = destKlass;
        klasses.add(current);
        while (!ABSENCE.equals(current.getParent())) {
            current = getKlass(current.getParent());
            klasses.add(current);
        }
        List<String> fields = new ArrayList<>();
        for (int i = klasses.size() - 1; i >= 0; i--) {
            fields.addAll(klasses.get(i).getObjectFieldNames());
        }

        return getInstanceObject(heap, fields, getInstanceKlassIndexByKlassName(klassName));
    }


    private int allocateArray(@Nonnull String arrayType, @Nonnull String valueType, int count) {
        int klassIndex = -1;
        if (arrayType.startsWith("[") && arrayType.endsWith(";")) {
            String klassName = arrayType.substring(arrayType.indexOf('L') + 1, arrayType.length() - 1);
            klassIndex = getInstanceKlassIndexByKlassName(klassName);
        }
        return getInstanceObjectReference(getInstanceObject(heap, arrayType, valueType, count, klassIndex));
    }

    private int allocateArrayOfRef(int cpIndex, int count) {
        String destKlassName = getKlassName(cpIndex);
        int klassIndex = getInstanceKlassIndexByKlassName(destKlassName);
        return getInstanceObjectReference(createArrayOfRef("[L" + destKlassName + ";", count, klassIndex));
    }

    private InstanceObject createArrayOfRef(String arrayType, int count, int klassIndex) {
        return getInstanceObject(heap, arrayType, JVMType.A.name(), count, klassIndex);
    }

    public void setExceptionDebugMode(boolean exceptionDebugMode) {
        this.exceptionDebugMode = exceptionDebugMode;
    }

    private void preserveDirectRefIndex(int index, @Nonnull Opcode opcode) {
        int counter = programCounter - 3;
        byteCode[counter++] = opcode.b();
        byteCode[counter++] = (byte) (index >> 8);
        byteCode[counter] = (byte) index;
    }

    private void invokeNonVirtualMethod(boolean quick, boolean staticMethod, @Nonnull Opcode opcode) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int methodIndex;
        if (quick) {
            methodIndex = index;
        } else {
            methodIndex = staticMethod ? getStaticMethodIndex(index) : getMethodIndex(index);
            preserveDirectRefIndexIfNeeded(methodIndex, staticMethod ? INVOKESTATIC_QUICK : INVOKESPECIAL_QUICK);
        }
        handleMethod(heap.getMethodRepo().getMethod(methodIndex), staticMethod, opcode);
    }

    private void invokeVirtualMethod(boolean quick, @Nonnull Opcode op) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int argSize;
        if (quick) {
            Method.DirectRef directRef = stackMethod[stackMethodPointer].getDirectRef(index);
            index = directRef.getIndex();
            argSize = directRef.getObjectRef();
        } else {
            argSize = getArgSize(index);
        }
        long reference = stack.getObjectRefBeforeInvoke(argSize);
        int klassIndex = getInstanceObjectByValue(reference, op).getKlassIndex();
        int virtualMethodIndex;
        if (quick) {
            virtualMethodIndex = index;
        } else {
            virtualMethodIndex = getVirtualMethodIndex(index, klassIndex);
            preserveDirectRefIndexIfNeeded(argSize, virtualMethodIndex, INVOKEVIRTUAL_QUICK);
        }
        int methodIndex = getInstanceKlassByIndex(klassIndex).getMethodIndex(virtualMethodIndex);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        handleMethod(method, false, op);
    }

    private void pushStaticFieldOntoStackFromInstanceObject(boolean quick) {
        handleStaticField((objRef, fieldValInd) -> stack.push(getInstanceObjectByRef(objRef).getValue(fieldValInd)), GETSTATIC_QUICK, quick);
    }

    private void putStaticFieldToInstanceObjectFromStack(boolean quick) {
        handleStaticField((objRef, fieldValInd) -> getInstanceObjectByRef(objRef).setValue(fieldValInd, stack.pop()), PUTSTATIC_QUICK, quick);
    }

    private void handleStaticField(BiConsumer<Integer, Integer> consumer, @Nonnull Opcode opcode, boolean quick) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int objectRef;
        int fieldValueIndex;
        if (quick) {
            Method.DirectRef directRef = stackMethod[stackMethodPointer].getDirectRef(index);
            objectRef = directRef.getObjectRef();
            fieldValueIndex = directRef.getIndex();
        } else {
            String klassFieldName = getKlassFieldName(index);
            InstanceKlass instanceKlass = getInstanceKlassByName(getKlassName(klassFieldName));
            objectRef = instanceKlass.getObjectRef();
            fieldValueIndex = instanceKlass.getIndexByFieldName(getFieldName(klassFieldName));
            preserveDirectRefIndexIfNeeded(objectRef, fieldValueIndex, opcode);
        }
        consumer.accept(objectRef, fieldValueIndex);
    }

    private void putFieldToInstanceObjectFromStack(boolean quick, @Nonnull Opcode op) {
        long value = stack.pop();
        handleField((object, fieldValueIndex) -> object.setValue(fieldValueIndex, value), PUTFIELD_QUICK, quick, op);
    }

    private void pushFieldOntoStackFromInstanceObject(boolean quick, @Nonnull Opcode op) {
        handleField((object, fieldValueIndex) -> stack.push(object.getValue(fieldValueIndex)), GETFIELD_QUICK, quick, op);
    }

    private void handleField(BiConsumer<InstanceObject, Integer> consumer, @Nonnull Opcode opcode, boolean quick, @Nonnull Opcode op) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        InstanceObject object = getInstanceObjectByValue(stack.pop(), op);
        int fieldValueIndex;
        if (quick) {
            fieldValueIndex = index;
        } else {
            fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(index)));
            preserveDirectRefIndexIfNeeded(fieldValueIndex, opcode);
        }
        consumer.accept(object, fieldValueIndex);
    }

    private void evaluateIntValueAndPushBackOntoStack(@Nonnull BiFunction<Integer, Integer, Integer> operation, @Nonnull Opcode op) {
        int first = getIntValue(stack.pop(), op);
        int second = getIntValue(stack.pop(), op);
        pushIntValueOntoStack(operation.apply(first, second));
    }

    private void compareValuesFromStack(@Nonnull Predicate<Integer> predicate, @Nonnull JVMType type, @Nonnull Opcode op) {
        int jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int first = getCheckedValue(stack.pop(), type, op);
        if (predicate.test(first)) {
            programCounter += jumpTo - 3;
        }
    }

    private void compareValuesFromStack(@Nonnull BiPredicate<Integer, Integer> predicate, @Nonnull JVMType type, @Nonnull Opcode op) {
        int jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int first = getCheckedValue(stack.pop(), type, op);
        int second = getCheckedValue(stack.pop(), type, op);
        if (predicate.test(first, second)) {
            programCounter += jumpTo - 3;
        }
    }

    private void pushOntoStackFromArray(@Nonnull Function<Long, Long> function, @Nonnull Opcode op) {
        int index = getPureValue(stack.pop());
        InstanceObject object = getInstanceObjectByValue(stack.pop(), op);
        checkArrayObject(object, op);
        stack.push(function.apply(object.getValue(index)));
    }

    private void storeToArrayFromStack(@Nonnull BiFunction<Long, InstanceObject, Long> function, @Nonnull JVMType type, @Nonnull Opcode op) {
        long value = checkValueType(stack.pop(), type, op);
        int index = getIntValue(stack.pop(), op);
        InstanceObject object = getInstanceObjectByValue(stack.pop(), op);
        checkArrayObject(object, op);
        object.setValue(index, function.apply(value, object));
    }

    private void preserveDirectRefIndexIfNeeded(int objectRef, int index, @Nonnull Opcode opcode) {
        if (symbolicRefResolution) {
            int directRefIndex = stackMethod[stackMethodPointer].addDirectRef(objectRef, index);
            preserveDirectRefIndex(directRefIndex, opcode);
        }
    }

    private void preserveDirectRefIndexIfNeeded(int index, @Nonnull Opcode opcode) {
        if (symbolicRefResolution) {
            preserveDirectRefIndex(index, opcode);
        }
    }

    private void handleMethod(@Nonnull Method method, boolean staticMethod, @Nonnull Opcode op) {
        if (method.isNative()) {
            invokeNativeMethod(method, op);
        } else {
            initNewMethod(method, staticMethod);
        }
    }

    private void initNewMethod(@Nonnull Method method, boolean staticMethod) {
        byteCode = method.getBytecode();
        klassName = method.getClassName();
        stackMethod[++stackMethodPointer] = method;
        stack.programCounter = programCounter;
        programCounter = 0;
        stack.initNewMethodStack(method.getArgSize() + (staticMethod ? 0 : 1), method.getVarSize(), method.getOperandSize());
    }

    private void destroyCurrentMethod(boolean returnValue) {
        Method method = stackMethod[--stackMethodPointer];
        stackMethod[stackMethodPointer + 1] = null;
        byteCode = method.getBytecode();
        klassName = method.getClassName();
        stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), returnValue);
        programCounter = stack.programCounter;
    }

    private void checkCast(@Nonnull Opcode op) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        int objectRef = getRefValue(stack.pop(), op);
        if (objectRef == NULL) {
            pushRefValueOntoStack(NULL);
        } else {
            InstanceObject object = getInstanceObjectByRef(objectRef);
            String castKlassName = getKlassName(index);
            if (object.isArray()) {
                if (castKlassName.equals(object.getArrayType())) {
                    pushRefValueOntoStack(objectRef);
                } else {
                    throw new ClassCastExceptionJVM(
                            Objects.requireNonNull(object.getArrayType()).replace("/", ".")
                                    + " cannot be cast to "
                                    + castKlassName.replace("/", "."));
                }
            } else {
                Klass klass = getKlass(getNameFromInstanceKlassByIndex(object.getKlassIndex()));
                if (checkCast(klass, castKlassName)) {
                    pushRefValueOntoStack(objectRef);
                } else {
                    throw new ClassCastExceptionJVM(
                            klass.getKlassName().replace("/", ".")
                                    + " cannot be cast to "
                                    + castKlassName.replace("/", "."));
                }
            }
        }
    }

    private void checkInstanceOf(@Nonnull Opcode op) {
        int index = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
        String className = getKlassName(index);
        int objectRef = getRefValue(stack.pop(), op);
        if (objectRef == NULL) {
            pushIntValueOntoStack(NULL);
            return;
        }
        InstanceObject object = getInstanceObjectByRef(objectRef);
        boolean instanceOf;
        if (object.isArray()) {
            instanceOf = className.equals(object.getArrayType());
        } else {
            Klass currentKlass = getKlass(getNameFromInstanceKlassByIndex(object.getKlassIndex()));
            instanceOf = checkCast(currentKlass, className);
        }
        pushIntValueOntoStack(instanceOf ? 1 : 0);
    }

    private int checkNotNull(int objectRef) {
        if (objectRef == NULL) {
            throw new NullPointerExceptionJVM();
        }
        return objectRef;
    }

}

