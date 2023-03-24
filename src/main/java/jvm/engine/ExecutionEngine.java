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

    private final static Opcode[] table = new Opcode[256];
    @Nonnull
    private final Heap heap;
    @Nonnull
    private final StackFrame stack;
    @Nonnull
    private final Method[] stackMethod;
    private int stackMethodPointer = 0;
    private byte[] byteCode;
    @Nonnull
    private String klassName = "java/lang/Object";
    private int programCounter;
    @Nonnull
    private Opcode currentOpcode;
    @Nonnull
    private static final Map<Integer, Object> nativeObjects = new HashMap<>();

    // VM Options
    private boolean exceptionDebugMode = true;

    private boolean symbolicRefResolution = true;

    StringBuilder instructions = new StringBuilder();


    public ExecutionEngine(@Nonnull Heap heap, @Nonnull StackFrame stackFrame) {
        this.heap = heap;
        this.stack = stackFrame;
        this.stackMethod = new Method[100];
        this.currentOpcode = NOP;
        for (Opcode op : values()) {
            table[op.getOpcode()] = op;
        }
    }

    private void init(@Nonnull Method method) {
        this.stackMethod[0] = method;
        this.byteCode = method.getBytecode();
        this.klassName = method.getClassName();
        this.stack.init(method.getVarSize(), method.getOperandSize());
        this.programCounter = 0;
        this.currentOpcode = NOP;
        logInitialization(method);
    }

    public long invoke(@Nonnull Method method) {

        init(method);
        while (true) {
            byte b = readByte();
            currentOpcode = table[b & 0xff];
            if (currentOpcode == null) {
                throw new RuntimeException("Unrecognised opcode byte: "
                        + (b & 0xff)
                        + " encountered at position "
                        + (programCounter - 1)
                        + "\n"
                        + "\n"
                        + getStackTrace(true));
            }
            if (exceptionDebugMode) {
                instructions.append(currentOpcode.name()).append(" ");
            }
            try {
                switch (currentOpcode) {
                    case ACONST_NULL:
                        // push the null object reference onto the operand stack
                        pushRefValueOntoStack(NULL);
                        break;
                    case ALOAD:
                        // The objectref in the local variable at index is pushed onto the operand stack
                        pushRefValueOntoStackFromLocalVar(readByte());
                        break;
                    case ALOAD_0:
                        pushRefValueOntoStackFromLocalVar(0);
                        break;
                    case ALOAD_1:
                        pushRefValueOntoStackFromLocalVar(1);
                        break;
                    case ALOAD_2:
                        pushRefValueOntoStackFromLocalVar(2);
                        break;
                    case ALOAD_3:
                        pushRefValueOntoStackFromLocalVar(3);
                        break;
                    case ILOAD:
                        //Load int from local variable to the operand stack
                        pushIntValueOntoStackFromLocalVar(readByte());
                        break;
                    case ILOAD_0:
                        pushIntValueOntoStackFromLocalVar(0);
                        break;
                    case ILOAD_1:
                        pushIntValueOntoStackFromLocalVar(1);
                        break;
                    case ILOAD_2:
                        pushIntValueOntoStackFromLocalVar(2);
                        break;
                    case ILOAD_3:
                        pushIntValueOntoStackFromLocalVar(3);
                        break;
                    case ASTORE:
                        setLocalRefValueFromStack(readByte());
                        break;
                    case ASTORE_0:
                        setLocalRefValueFromStack(0);
                        break;
                    case ASTORE_1:
                        setLocalRefValueFromStack(1);
                        break;
                    case ASTORE_2:
                        setLocalRefValueFromStack(2);
                        break;
                    case ASTORE_3:
                        setLocalRefValueFromStack(3);
                        break;
                    case ISTORE:
                        setLocalIntValueFromStack(readByte());
                        break;
                    case ISTORE_0:
                        setLocalIntValueFromStack(0);
                        break;
                    case ISTORE_1:
                        setLocalIntValueFromStack(1);
                        break;
                    case ISTORE_2:
                        setLocalIntValueFromStack(2);
                        break;
                    case ISTORE_3:
                        setLocalIntValueFromStack(3);
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
                        pushIntValueOntoStack(readByte());
                        break;
                    case DUP:
                        //Duplicate the top operand stack value
                        stack.dup();
                        break;
                    case DUP_X1:
                        stack.dupX1();
                        break;
                    case GETFIELD:
                        pushFieldOntoStackFromInstanceObject(false);
                        break;
                    case GETFIELD_QUICK:
                        pushFieldOntoStackFromInstanceObject(true);
                        break;
                    case GETSTATIC:
                        pushStaticFieldOntoStackFromInstanceObject(false);
                        break;
                    case GETSTATIC_QUICK:
                        pushStaticFieldOntoStackFromInstanceObject(true);
                        break;
                    case PUTFIELD:
                        putFieldToInstanceObjectFromStack(false);
                        break;
                    case PUTFIELD_QUICK:
                        putFieldToInstanceObjectFromStack(true);
                        break;
                    case PUTSTATIC:
                        putStaticFieldToInstanceObjectFromStack(false);
                        break;
                    case PUTSTATIC_QUICK:
                        putStaticFieldToInstanceObjectFromStack(true);
                        break;
                    case CHECKCAST:
                        checkCast(false);
                        break;
                    case CHECKCAST_QUICK:
                        checkCast(true);
                        break;
                    case GOTO:
                        programCounter += (byteCode[programCounter] << 8) + (byteCode[programCounter + 1] & 0xff) - 1;
                        break;
                    case INSTANCEOF:
                        checkInstanceOf(false);
                        break;
                    case INSTANCEOF_QUICK:
                        checkInstanceOf(true);
                        break;
                    case IADD:
                        evaluateIntValueAndPushBackOntoStack(Integer::sum);
                        break;
                    case IAND:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal & firstVal);
                        break;
                    case ISHL:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal << firstVal);
                        break;
                    case ISHR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal >> firstVal);
                        break;
                    case IUSHR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal >>> firstVal);
                        break;
                    case IXOR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> ~secondVal);
                        break;
                    case IDIV:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> {
                            if (firstVal == 0) {
                                throw new ArithmeticException("cannot divide 0");
                            }
                            return secondVal / firstVal;
                        });
                        break;
                    case IMUL:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> firstVal * secondVal);
                        break;
                    case ISUB:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal - firstVal);
                        break;
                    case IOR:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> firstVal | secondVal);
                        break;
                    case IREM:
                        evaluateIntValueAndPushBackOntoStack((firstVal, secondVal) -> secondVal % firstVal);
                        break;
                    case IF_ACMPNE:
                        compareValuesFromStack((firstVal, secondVal) -> !Objects.equals(secondVal, firstVal), JVMType.A);
                        break;
                    case IF_ACMPEQ:
                        compareValuesFromStack((firstVal, secondVal) -> Objects.equals(secondVal, firstVal), JVMType.A);
                        break;
                    case IF_ICMPEQ:
                        compareValuesFromStack((firstVal, secondVal) -> Objects.equals(secondVal, firstVal), JVMType.I);
                        break;
                    case IF_ICMPNE:
                        compareValuesFromStack((firstVal, secondVal) -> !Objects.equals(secondVal, firstVal), JVMType.I);
                        break;
                    case IF_ICMPLT:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal < firstVal, JVMType.I);
                        break;
                    case IF_ICMPGT:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal > firstVal, JVMType.I);
                        break;
                    case IF_ICMPGE:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal >= firstVal, JVMType.I);
                        break;
                    case IF_ICMPLE:
                        compareValuesFromStack((firstVal, secondVal) -> secondVal <= firstVal, JVMType.I);
                        break;
                    case IFEQ:
                        compareValuesFromStack(val -> val == 0, JVMType.I);
                        break;
                    case IFGE:
                        compareValuesFromStack(val -> val >= 0, JVMType.I);
                        break;
                    case IFGT:
                        compareValuesFromStack(val -> val > 0, JVMType.I);
                        break;
                    case IFLE:
                        compareValuesFromStack(val -> val <= 0, JVMType.I);
                        break;
                    case IFLT:
                        compareValuesFromStack(val -> val < 0, JVMType.I);
                        break;
                    case IFNE:
                        compareValuesFromStack(val -> val != 0, JVMType.I);
                        break;
                    case IFNONNULL:
                        compareValuesFromStack(val -> val != 0, JVMType.A);
                        break;
                    case IFNULL:
                        compareValuesFromStack(val -> val == 0, JVMType.A);
                        break;
                    case IINC:
                        /*
                         *The index is an unsigned byte that must be an index into the local variable array of the current frame.
                         * The const is an immediate signed byte. The local variable at index must contain an int.
                         * The value const is first sign-extended to an int, and then the local variable at index is incremented by that amount.
                         * */
                        int index = readByte();
                        stack.setLocalVar(index, setIntValueType(getIntValue(stack.getLocalVar(index)) + readByte()));
                        break;
                    case I2C:
                        stack.push(checkValueType(stack.pop(), JVMType.I));
                        break;
                    case INEG:
                        pushIntValueOntoStack(-getIntValue(stack.pop()));
                        break;
                    case INVOKESPECIAL:
                        invokeNonVirtualMethod(false, false);
                        break;
                    case INVOKESPECIAL_QUICK:
                        invokeNonVirtualMethod(true, false);
                        break;
                    case INVOKESTATIC:
                        invokeNonVirtualMethod(false, true);
                        break;
                    case INVOKESTATIC_QUICK:
                        invokeNonVirtualMethod(true, true);
                        break;
                    case INVOKEVIRTUAL:
                        invokeVirtualMethod(false);
                        break;
                    case INVOKEVIRTUAL_QUICK:
                        invokeVirtualMethod(true);
                        break;
                    case ARETURN:
                        if (stack.invokeCount == 0) {
                            return getRefValue(stack.pop());
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
                        pushRefValueOntoStack(allocateInstanceObjectAndGetReference(false));
                        break;
                    case NEW_QUICK:
                        pushRefValueOntoStack(allocateInstanceObjectAndGetReference(true));
                        break;
                    case NEWARRAY:
                        int atype = readByte();
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
                        pushRefValueOntoStack(allocateArray(JVMType.values()[atype - 3],
                                getIntValue(stack.pop()), atype - 4));
                        break;
                    case ANEWARRAY:
                        pushRefValueOntoStack(allocateReferenceArray(false));
                        break;
                    case ANEWARRAY_QUICK:
                        pushRefValueOntoStack(allocateReferenceArray(true));
                        break;
                    case MULTIANEWARRAY:
                        newMultiArray(false);
                        break;
                    case MULTIANEWARRAY_QUICK:
                        newMultiArray(true);
                        break;
                    case ARRAYLENGTH:
                        pushIntValueOntoStack(checkArrayObject(getInstanceObjectByValue(stack.pop())).getFieldCount());
                        break;
                    case AALOAD:
                        pushOntoStackFromArray(val -> checkValueType(val, JVMType.A));
                        break;
                    case IALOAD:
                        pushOntoStackFromArray(val -> checkValueType(val, JVMType.I));
                        break;
                    case BALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type byte or of type boolean.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The byte value in the component of the array at index is retrieved, sign-extended to an int value, and pushed onto the top of the operand stack.
                         */
                        pushOntoStackFromArray(val -> setIntValueType(getPureValue(checkByteOrBooleanValueType(val))));
                        break;
                    case CALOAD:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index must be of type int. Both arrayref and index are popped from the operand stack.
                         * The component of the array at index is retrieved and zero-extended to an int value. That value is pushed onto the operand stack.
                         */
                        pushOntoStackFromArray(val -> setIntValueType(getPureValue(checkValueType(val, JVMType.C))));
                        break;
                    case AASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type reference.
                         * The index must be of type int and value must be of type reference. The arrayref, index, and value are popped from the operand stack.
                         * The reference value is stored as the component of the array at index.
                         */
                        storeToArrayFromStack((val, obj) -> val, JVMType.A);
                        break;
                    case IASTORE:
                        /*
                         * Store into int array
                         * Both index and value must be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> val, JVMType.I);
                        break;
                    case BASTORE:
                        /*
                         * Store into byte or boolean array
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a byte and stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> {
                            JVMType type = JVMType.values()[obj.getKlassIndex() + 1];
                            if (type == JVMType.Z || type == JVMType.B) {
                                return setValueType(getPureValue(val), type);
                            } else {
                                throw new RuntimeException("Wrong type of array\n" + getStackTrace(false));
                            }
                        }, JVMType.I);
                        break;
                    case CASTORE:
                        /*
                         * The arrayref must be of type reference and must refer to an array whose components are of type char.
                         * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                         * The int value is truncated to a char and stored as the component of the array indexed by index.
                         */
                        storeToArrayFromStack((val, obj) -> setCharValueType(getIntValue(val)), JVMType.I);
                        break;
                    case POP:
                        stack.pop();
                        break;
                    case RET:
                        throw new IllegalArgumentException("Illegal opcode byte: " + (b & 0xff) + " encountered at position " + (programCounter - 1) + ". Stopping.");
                    case SIPUSH:
                        pushIntValueOntoStack(readTwoBytes());
                        break;
                    case SWAP:
                        long firstVal = stack.pop();
                        long secondVal = stack.pop();
                        stack.push(firstVal);
                        stack.push(secondVal);
                        break;
                    case LDC:
                        ConstantPoolEntry entry = getSourceKlass().getCPItem(readByte() - 1);
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
                        System.err.println("Saw " + currentOpcode + " - that can't happen. Stopping.");
                        System.err.println(getStackTrace(true));
                        System.exit(1);
                }
            } catch (RuntimeExceptionJVM e) {
                if (exceptionDebugMode) {
                    if (e instanceof ClassCastExceptionJVM) {
                        throw new ClassCastExceptionJVM(e.getLocalizedMessage() + "\n" + getStackTrace(false));
                    } else if (e instanceof NullPointerExceptionJVM) {
                        throw new NullPointerExceptionJVM("\n" + getStackTrace(false));
                    } else if (e instanceof OutOfMemoryErrorJVM) {
                        throw new OutOfMemoryErrorJVM("\n" + getStackTrace(false));
                    } else if (e instanceof ClassNotFoundExceptionJVM) {
                        throw new ClassNotFoundExceptionJVM(e.getLocalizedMessage() + "\n" + getStackTrace(false));
                    } else {
                        throw e;
                    }
                } else {
                    System.out.println(Utils.changeJVMKlassNameToSystemKlassName(e.toString()) + "\n" + getStackTrace(false));
                    System.exit(-1);
                }
            } catch (Exception e) {
                throw new RuntimeException("\n" + getStackTrace(false) + "\n\n" + e);
            }
        }
    }

    @Nonnull
    private String getStackTrace(boolean showMnemonics) {
        StringBuilder stackTrace = new StringBuilder();
        for (int i = stackMethodPointer; i >= 0; i--) {
            Method method = stackMethod[i];
            stackTrace.append("\tat ")
                    .append(method.getClassName())
                    .append(".")
                    .append(method.getNameAndType())
                    .append(i == stackMethodPointer ? " " + currentOpcode : "")
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

    private void invokeNativeMethod(@Nonnull Method method) {
        String methodName = method.getClassName() + "." + method.getNameAndType();
        switch (methodName) {
            case HASHCODE: {
                InstanceObject object1 = getInstanceObjectByValue(stack.pop());
                pushIntValueOntoStack(Objects.hashCode(object1));
                break;
            }
            case TO_STRING: {
                int objectRef = getRefValue(stack.pop());
                InstanceObject object1 = getInstanceObjectByRef(objectRef);
                String result = getNameFromInstanceKlassByIndex(object1.getKlassIndex()).replace('/', '.')
                        + "@"
                        + Integer.toHexString(objectRef);
                createStringInstance(result, false);
                break;
            }
            case STRING_PRINTLN: {
                int stringObjectRef = getRefValue(stack.pop());
                System.out.println(getString(stringObjectRef));
                stack.pop();
                break;
            }
            case STRING_PRINT: {
                int stringObjectRef = getRefValue(stack.pop());
                System.out.print(getString(stringObjectRef));
                stack.pop();
                break;
            }
            case CHAR_PRINT:
                System.out.print((char) getIntValue(stack.pop()));
                stack.pop();
                break;
            case CHAR_PRINTLN:
                System.out.println((char) getIntValue(stack.pop()));
                stack.pop();
                break;
            case INT_PRINT:
                System.out.print(getIntValue(stack.pop()));
                stack.pop();
                break;
            case INT_PRINTLN:
                System.out.println(getIntValue(stack.pop()));
                stack.pop();
                break;
            case INIT_SOCKET: {
                int socketObjRef = getRefValue(stack.getLocalVar(0));
                int stringObjRef = getRefValue(stack.getLocalVar(1));
                int port = getIntValue(stack.getLocalVar(2));
                String ipAddress = getString(stringObjRef);
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
                int socketObjRef = getRefValue(stack.pop());
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
                int inputStreamObjRef = getRefValue(stack.pop());
                int inputStreamReaderObjRef = getRefValue(stack.pop());
                InputStream inputStream = (InputStream) nativeObjects.get(inputStreamObjRef);
                nativeObjects.put(inputStreamReaderObjRef, new InputStreamReader(inputStream));
                break;
            }
            case INIT_BUFFERED_READER: {
                int inputStreamReaderObjRef = getRefValue(stack.pop());
                int bufferedReaderObjRef = getRefValue(stack.pop());
                Reader inputStreamReader = (Reader) nativeObjects.get(inputStreamReaderObjRef);
                nativeObjects.put(bufferedReaderObjRef, new BufferedReader(inputStreamReader));
                break;
            }
            case INIT_PRINT_WRITER: {
                int bool = getIntValue(stack.pop());
                int outputStreamObjRef = getRefValue(stack.pop());
                int printWriterObjRef = getRefValue(stack.pop());
                OutputStream outputStream = (OutputStream) nativeObjects.get(outputStreamObjRef);
                nativeObjects.put(printWriterObjRef, new PrintWriter(outputStream, bool == 1));
                break;
            }
            case PRINT_WRITER_PRINTLN: {
                int stringObjRef = getRefValue(stack.pop());
                String message = getString(stringObjRef);
                int printWriterObjRef = getRefValue(stack.pop());
                PrintWriter printWriter = (PrintWriter) nativeObjects.get(printWriterObjRef);
                printWriter.println(message);
                break;
            }
            case READ_LINE: {
                int bufferedReaderObjRef = getRefValue(stack.pop());
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
                int bound = getIntValue(stack.pop());
                pushIntValueOntoStack(new Random().nextInt(bound));
                break;
            case STRING_INTERN:
                int stringObjRef = getRefValue(stack.pop());
                String str = getString(stringObjRef);
                createStringInstance(str, true);
                break;
        }
    }

    private int readTwoBytes() {
        return (readByte() << 8) + (readByte() & 0xff);
    }

    private byte readByte() {
        return byteCode[programCounter++];
    }

    @Nonnull
    private String getString(int objectRef) {
        InstanceObject stringObject = getInstanceObjectByRef(objectRef);
        InstanceObject charArrayObject = getInstanceObjectByValue(
                stringObject.getFieldValue(stringObject.getIndexByFieldName("value:[C")));
        char[] buf = new char[charArrayObject.getFieldCount()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) charArrayObject.getFieldValue(i);
        }
        return String.valueOf(buf);
    }

    private void createMultiArray(int indexDim, int[] dimensions, InstanceObject object, String type) {
        String arrayType = type.substring(type.indexOf('[') + 1);
        for (int i = 0; i < object.getFieldCount(); i++) {
            if (indexDim == dimensions.length - 1) {
                boolean refType = (arrayType.startsWith("[") && arrayType.endsWith(";"));
                int arrayKlassIndex = getArrayKlassIndex(arrayType, refType);
                object.setFieldValue(i, setRefValueType(
                        allocateArray(refType ? JVMType.A : JVMType.values()[arrayKlassIndex + 1], dimensions[indexDim], arrayKlassIndex)));
            } else {
                InstanceObject newObject = createReferenceArray(dimensions[indexDim], setArrayInstanceKlass(arrayType));
                object.setFieldValue(i, setRefValueType(getInstanceObjectReference(newObject)));
                createMultiArray(indexDim + 1, dimensions, newObject, arrayType);
            }
        }
    }

    private int getArrayKlassIndex(@Nonnull String arrayType, boolean refType) {
        if (refType) {
            return setArrayInstanceKlass(arrayType);
        }
        String aType = arrayType.substring(1);
        switch (aType) {
            case "Z":
                return 0;
            case "C":
                return 1;
            case "I":
                return 6;
            default:
                throw new RuntimeException("Unknown array type :" + aType);
        }
    }

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

    private long checkValueType(long value, @Nonnull JVMType type) {
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
                    + getStackTrace(false));
        }
        return value;
    }

    private long checkByteOrBooleanValueType(long value) {
        if (getValueType(value) == JVMType.Z.ordinal() || getValueType(value) == JVMType.B.ordinal()) {
            return value;
        }
        throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(value)] + " is not equal " + "byte or boolean");
    }

    @Nonnull
    private InstanceObject checkArrayObject(@Nonnull InstanceObject object) {
        if (!object.isArray()) {
            throw new RuntimeException("Object is not array\n" + getStackTrace(false));
        }
        return object;
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

    private void pushRefValueOntoStackFromLocalVar(int index) {
        stack.push(checkValueType(stack.getLocalVar(index), JVMType.A));
    }

    private void pushIntValueOntoStackFromLocalVar(int index) {
        stack.push(checkValueType(stack.getLocalVar(index), JVMType.I));
    }

    private void setLocalRefValueFromStack(int index) {
        stack.setLocalVar(index, checkValueType(stack.pop(), JVMType.A));
    }

    private void setLocalIntValueFromStack(int index) {
        stack.setLocalVar(index, checkValueType(stack.pop(), JVMType.I));
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getPureValue(long value) {
        return (int) value;
    }

    private int getIntValue(long value) {
        return getValue(value, JVMType.I);
    }

    private int getRefValue(long value) {
        return getValue(value, JVMType.A);
    }

    private int getValue(long value, JVMType type) {
        return getPureValue(checkValueType(value, type));
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
    private InstanceObject getInstanceObjectByValue(long value) {
        return getInstanceObjectByRef(getRefValue(value));
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
        return instanceKlass.getIndexByStaticMethodName(getMethodName(klassMethodName));
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
        int charArrayRef = allocateArray(JVMType.C, str.length(), JVMType.C.ordinal() - 1);
        /*----------------------------------*/
        InstanceObject charArrayObj = getInstanceObjectByRef(charArrayRef);
        for (int i = 0; i < str.length(); i++) {
            charArrayObj.setFieldValue(i, setCharValueType(str.charAt(i)));
        }
        stringObj.setFieldValue(stringObj.getIndexByFieldName("value:[C"), setRefValueType(charArrayRef));

        if (heap.isEnabledCacheString() && toPoolOfStrings) {
            heap.putStringRefToPool(str, objRef, charArrayRef);
        }
    }

    private int allocateInstanceObjectAndGetReference(boolean quick) {
        return allocateInstanceObjectAndGetReference(getResolvedString(quick, readTwoBytes(), NEW_QUICK));
    }

    @Nonnull
    private String getResolvedString(boolean quick, int index, @Nonnull Opcode opcode) {
        String result;
        if (quick) {
            result = getDirectRef(index).getString();
        } else {
            result = getKlassName(index);
            preserveStringIfNeeded(result, opcode);
        }
        return result;
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


    private int allocateArray(@Nonnull JVMType valueType, int count, int klassIndex) {
        return getInstanceObjectReference(getInstanceObject(heap, valueType, count, klassIndex));
    }

    private int allocateReferenceArray(boolean quick) {
        int klassIndex;
        if (quick) {
            klassIndex = readTwoBytes();
        } else {
            klassIndex = setArrayInstanceKlass("[L" + getKlassName(readTwoBytes()) + ";");
            preserveIndexIfNeeded(klassIndex, ANEWARRAY_QUICK);
        }
        return getInstanceObjectReference(createReferenceArray(getIntValue(stack.pop()), klassIndex));
    }

    private InstanceObject createReferenceArray(int count, int klassIndex) {
        return getInstanceObject(heap, JVMType.A, count, klassIndex);
    }

    private void newMultiArray(boolean quick) {
        String arrayType = getResolvedString(quick, readTwoBytes(), MULTIANEWARRAY_QUICK);
        int[] dimensions = new int[readByte()];
        for (int i = dimensions.length - 1; i >= 0; i--) {
            dimensions[i] = getIntValue(stack.pop());
        }
        InstanceObject object = createReferenceArray(dimensions[0], setArrayInstanceKlass(arrayType));
        pushRefValueOntoStack(getInstanceObjectReference(object));
        createMultiArray(1, dimensions, object, arrayType);
    }

    private int setArrayInstanceKlass(@Nonnull String array) {
        Integer index = heap.getKlassLoader().getInstanceKlassIndexByName(array, false);
        if (index != null) {
            return index;
        }
        heap.getKlassLoader().initArrayKlass(array);
        return setArrayInstanceKlass(array);
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

    private void invokeNonVirtualMethod(boolean quick, boolean staticMethod) {
        int index = readTwoBytes();
        int methodIndex;
        if (quick) {
            methodIndex = index;
        } else {
            methodIndex = staticMethod ? getStaticMethodIndex(index) : getMethodIndex(index);
            preserveIndexIfNeeded(methodIndex, staticMethod ? INVOKESTATIC_QUICK : INVOKESPECIAL_QUICK);
        }
        handleMethod(heap.getMethodRepo().getMethod(methodIndex), staticMethod);
    }

    private void invokeVirtualMethod(boolean quick) {
        int index = readTwoBytes();
        int argSize;
        if (quick) {
            Method.DirectRef directRef = getDirectRef(index);
            index = directRef.getSecondIndex();
            argSize = directRef.getFirstIndex();
        } else {
            argSize = getArgSize(index);
        }
        long reference = stack.getObjectRefBeforeInvoke(argSize);
        int klassIndex = getInstanceObjectByValue(reference).getKlassIndex();
        int virtualMethodIndex;
        if (quick) {
            virtualMethodIndex = index;
        } else {
            virtualMethodIndex = getVirtualMethodIndex(index, klassIndex);
            preserveDirectRefIfNeeded(argSize, virtualMethodIndex, INVOKEVIRTUAL_QUICK);
        }
        int methodIndex = getInstanceKlassByIndex(klassIndex).getMethodIndex(virtualMethodIndex);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        handleMethod(method, false);
    }

    private void pushStaticFieldOntoStackFromInstanceObject(boolean quick) {
        handleStaticField((objRef, fieldValInd) -> stack.push(getInstanceObjectByRef(objRef).getFieldValue(fieldValInd)), GETSTATIC_QUICK, quick);
    }

    private void putStaticFieldToInstanceObjectFromStack(boolean quick) {
        handleStaticField((objRef, fieldValInd) -> getInstanceObjectByRef(objRef).setFieldValue(fieldValInd, stack.pop()), PUTSTATIC_QUICK, quick);
    }

    private void handleStaticField(BiConsumer<Integer, Integer> consumer, @Nonnull Opcode opcode, boolean quick) {
        int objectRef;
        int fieldValueIndex;
        if (quick) {
            Method.DirectRef directRef = getDirectRef(readTwoBytes());
            objectRef = directRef.getFirstIndex();
            fieldValueIndex = directRef.getSecondIndex();
        } else {
            String klassFieldName = getKlassFieldName(readTwoBytes());
            InstanceKlass instanceKlass = getInstanceKlassByName(getKlassName(klassFieldName));
            objectRef = instanceKlass.getObjectRef();
            fieldValueIndex = instanceKlass.getIndexByStaticFieldName(getFieldName(klassFieldName));
            preserveDirectRefIfNeeded(objectRef, fieldValueIndex, opcode);
        }
        consumer.accept(objectRef, fieldValueIndex);
    }

    private void putFieldToInstanceObjectFromStack(boolean quick) {
        long value = stack.pop();
        handleField((object, fieldValueIndex) -> object.setFieldValue(fieldValueIndex, value), PUTFIELD_QUICK, quick);
    }

    private void pushFieldOntoStackFromInstanceObject(boolean quick) {
        handleField((object, fieldValueIndex) -> stack.push(object.getFieldValue(fieldValueIndex)), GETFIELD_QUICK, quick);
    }

    private void handleField(BiConsumer<InstanceObject, Integer> consumer, @Nonnull Opcode opcode, boolean quick) {
        int index = readTwoBytes();
        InstanceObject object = getInstanceObjectByValue(stack.pop());
        int fieldValueIndex;
        if (quick) {
            fieldValueIndex = index;
        } else {
            fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(index)));
            preserveIndexIfNeeded(fieldValueIndex, opcode);
        }
        consumer.accept(object, fieldValueIndex);
    }

    private void evaluateIntValueAndPushBackOntoStack(@Nonnull BiFunction<Integer, Integer, Integer> operation) {
        int first = getIntValue(stack.pop());
        int second = getIntValue(stack.pop());
        pushIntValueOntoStack(operation.apply(first, second));
    }

    private void compareValuesFromStack(@Nonnull Predicate<Integer> predicate, @Nonnull JVMType type) {
        int jumpTo = readTwoBytes();
        int first = getValue(stack.pop(), type);
        if (predicate.test(first)) {
            programCounter += jumpTo - 3;
        }
    }

    private void compareValuesFromStack(@Nonnull BiPredicate<Integer, Integer> predicate, @Nonnull JVMType type) {
        int jumpTo = readTwoBytes();
        int first = getValue(stack.pop(), type);
        int second = getValue(stack.pop(), type);
        if (predicate.test(first, second)) {
            programCounter += jumpTo - 3;
        }
    }

    private void pushOntoStackFromArray(@Nonnull Function<Long, Long> function) {
        int index = getIntValue(stack.pop());
        InstanceObject object = checkArrayObject(getInstanceObjectByValue(stack.pop()));
        stack.push(function.apply(object.getFieldValue(index)));
    }

    private void storeToArrayFromStack(@Nonnull BiFunction<Long, InstanceObject, Long> function, @Nonnull JVMType type) {
        long value = checkValueType(stack.pop(), type);
        int index = getIntValue(stack.pop());
        InstanceObject object = checkArrayObject(getInstanceObjectByValue(stack.pop()));
        object.setFieldValue(index, function.apply(value, object));
    }

    private void preserveDirectRefIfNeeded(int first, int second, @Nonnull Opcode opcode) {
        preserveIfNeeded(method -> method.builder()
                .addFirstIndex(first)
                .addSecondIndex(second)
                .buildDirectRefIndex(), opcode);
    }

    private void preserveIndexIfNeeded(int index, @Nonnull Opcode opcode) {
        if (symbolicRefResolution) {
            preserveDirectRefIndex(index, opcode);
        }
    }

    private void preserveStringIfNeeded(@Nonnull String str, @Nonnull Opcode opcode) {
        preserveIfNeeded(method -> method.builder().addString(str).buildDirectRefIndex(), opcode);
    }

    private void preserveIfNeeded(@Nonnull Function<Method, Integer> function, @Nonnull Opcode opcode) {
        if (symbolicRefResolution) {
            int directRefIndex = function.apply(getCurrentMethod());
            preserveDirectRefIndex(directRefIndex, opcode);
        }
    }

    @Nonnull
    private Method getCurrentMethod() {
        return stackMethod[stackMethodPointer];
    }

    @Nonnull
    private Method.DirectRef getDirectRef(int index) {
        return getCurrentMethod().getDirectRef(index);
    }

    private void handleMethod(@Nonnull Method method, boolean staticMethod) {
        if (method.isNative()) {
            invokeNativeMethod(method);
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
        logInitialization(method);

    }

    private void logInitialization(@Nonnull Method method) {
        if (exceptionDebugMode) {
            instructions.append("\nINIT - ")
                    .append(method.getClassName())
                    .append("#")
                    .append(method.getNameAndType())
                    .append("\n");
        }
    }

    private void destroyCurrentMethod(boolean returnValue) {
        Method method = stackMethod[--stackMethodPointer];
        stackMethod[stackMethodPointer + 1] = null;
        byteCode = method.getBytecode();
        klassName = method.getClassName();
        stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), returnValue);
        programCounter = stack.programCounter;
        if (exceptionDebugMode) {
            instructions.append("\nCONTINUE - ")
                    .append(method.getClassName())
                    .append("#")
                    .append(method.getNameAndType())
                    .append("\n");
        }
    }

    private void checkCast(boolean quick) {
        int index = readTwoBytes();
        int objectRef = getRefValue(stack.pop());
        if (objectRef == NULL) {
            pushRefValueOntoStack(NULL);
        } else {
            String castKlassName;
            int castKlassNameIndex;
            if (quick) {
                castKlassNameIndex = index;
                castKlassName = getInstanceKlassByIndex(castKlassNameIndex).getName();
            } else {
                castKlassName = getKlassName(index);
                castKlassNameIndex = getInstanceKlassIndexByKlassName(castKlassName);
                preserveIndexIfNeeded(castKlassNameIndex, CHECKCAST_QUICK);
            }
            InstanceObject object = getInstanceObjectByRef(objectRef);
            if (object.isArray()) {
                if (castKlassNameIndex == object.getKlassIndex()) {
                    pushRefValueOntoStack(objectRef);
                } else {
                    throw new ClassCastExceptionJVM(
                            getNameFromInstanceKlassByIndex(object.getKlassIndex()).replace("/", ".")
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

    private void checkInstanceOf(boolean quick) {
        String className = getResolvedString(quick, readTwoBytes(), INSTANCEOF_QUICK);
        int objectRef = getRefValue(stack.pop());
        if (objectRef == NULL) {
            pushIntValueOntoStack(NULL);
            return;
        }
        InstanceObject object = getInstanceObjectByRef(objectRef);
        boolean instanceOf;
        if (object.isArray()) {
            instanceOf = className.equals(getNameFromInstanceKlassByIndex(object.getKlassIndex()));
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

