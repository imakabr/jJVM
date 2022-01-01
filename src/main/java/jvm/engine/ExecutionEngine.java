package jvm.engine;

import jvm.JVMType;
import jvm.heap.*;
import jvm.lang.KlassCastException;
import jvm.parser.Method;
import jvm.parser.Klass;
import jvm.parser.ConstantPoolEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jvm.engine.Opcode.*;
import static jvm.heap.KlassLoader.*;

public final class ExecutionEngine {

    private final static int NULL = 0;
    private final static String HASHCODE = "hashCode:()I";
    private final static String STRING_PRINTLN = "println:(Ljava/lang/String;)V";
    private final static String STRING_PRINT = "print:(Ljava/lang/String;)V";
    private final static String CHAR_PRINTLN = "println:(C)V";
    private final static String CHAR_PRINT = "print:(C)V";
    private final static String INT_PRINTLN = "println:(I)V";
    private final static String INT_PRINT = "print:(I)V";

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
                        + getStackTrace(stackMethod, stackMethodPointer, null, true));
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
                        return getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op));
                    }
                    method = stackMethod[--stackMethodPointer];
                    stackMethod[stackMethodPointer + 1] = null;
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), true);
                    programCounter = stack.programCounter;
                    break;
                case ASTORE:
                    reference = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
                    stack.setLocalVar(byteCode[programCounter++], reference);
                    break;
                case ASTORE_0:
                    reference = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
                    stack.setLocalVar(0, reference);
                    break;
                case ASTORE_1:
                    reference = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
                    stack.setLocalVar(1, reference);
                    break;
                case ASTORE_2:
                    reference = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
                    stack.setLocalVar(2, reference);
                    break;
                case ASTORE_3:
                    reference = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
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
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                    stack.push(object.getValue(fieldValueIndex));
                    break;
                case GETSTATIC:
                    cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    //---------------------------------------------------------------------------------

                    // todo restore indexes for resolving
                    objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                    fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));

                    //---------------------------------------------------------------------------------
                    stack.push(heap.getInstanceObject(objectRef).getValue(fieldValueIndex)); // to do deal with type to JVMValue
                    break;
                //----------------------------------------------------------------------------------------------------------------------
                case CHECKCAST:
                    cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    objectRef = getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op));
                    object = heap.getInstanceObject(objectRef);
                    String castKlassName = heap.getKlassLoader().getLoadedKlassByName(klassName).getKlassNameByCPIndex((short) cpLookup);
                    Klass klass = heap.getKlassLoader().getLoadedKlassByName(heap.getInstanceKlass(object.getKlassIndex()).getName());
                    if (!checkCast(klass, castKlassName)) {
                        throw new KlassCastException("\n"
                                + klass.getKlassName().replace("/", ".")
                                + " can not cast to "
                                + castKlassName.replace("/", ".")
                                + "\n"
                                + "\n"
                                + getStackTrace(stackMethod, stackMethodPointer, null, false));
                    }
                    stack.push(setRefValueType(objectRef));
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
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (second == 0) throw new ArithmeticException("cannot divide 0");
                    stack.push(setIntValueType(second / first));
                    break;
                case IF_ACMPNE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op))
                            != getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ACMPEQ:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op))
                            == getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPEQ:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op))
                            == getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPNE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op))
                            != getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op))) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPLT:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (second < first) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPGT:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (second > first) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPGE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (second >= first) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IF_ICMPLE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (second <= first) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFEQ:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first == 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFGE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first >= 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFGT:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first > 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFLE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first <= 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFLT:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first < 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFNE:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    if (first != 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;

                //---------------------------------------------------------------------------------------------------------------------------
                case IFNONNULL:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)) != 0) {
                        programCounter += jumpTo - 3;
                    }
                    break;
                case IFNULL:
                    jumpTo = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    if (getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)) == 0) {
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
                    int index = byteCode[programCounter++];
                    first = getPureValue(checkValueType(stack.getLocalVar(index), JVMType.I, stackMethod, stackMethodPointer, op));
                    stack.setLocalVar(index, setIntValueType(first + byteCode[programCounter++]));
                    break;
                case I2C:
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
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
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    stack.push(setIntValueType((long) first * second));
                    break;
                case INEG:
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    stack.push(setIntValueType(-first));
                    break;
                //------------------------------------------------------------------------------------------------------------------------------------------
                case INVOKESPECIAL:
                    cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
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
                    cpLookup = (byteCode[programCounter++] << 8) + (byteCode[programCounter++] & 0xff);
                    int klassIndex = heap.getInstanceObject(
                                    getPureValue(
                                            checkValueType(
                                                    stack.getObjectRefBeforeInvoke(getArgSize(klassName, cpLookup)), JVMType.A,
                                                    stackMethod, stackMethodPointer, op)))
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
                        invokeNativeMethod(stack, method, stackMethod, stackMethodPointer, op);
                    }
                    break;
                //------------------------------------------------------------------------------------------------------------------------------------------
                case IOR:
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    stack.push(setIntValueType(first | second));
                    break;
                case IREM:
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
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
                    first = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    second = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
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
                    stack.push(setRefValueType(allocateArray(
                            JVMType.values()[atype - 4].name(),
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
                        dimensions[i] = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    }
                    object = allocateArrayOfRef(dimensions[0], -1);
                    stack.push(setRefValueType(heap.getObjectRef(object)));
                    createMultiArray(1, dimensions, object, getValueType(klassName, cpLookup));
                    break;
                case ARRAYLENGTH:
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    stack.push(setIntValueType(object.size()));
                    break;
                case AALOAD:
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    stack.push(checkValueType(object.getValue(index), JVMType.A, stackMethod, stackMethodPointer, op));
                    break;
                case IALOAD:
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    stack.push(checkValueType(object.getValue(index), JVMType.I, stackMethod, stackMethodPointer, op));
                    break;
                case BALOAD:
                    /*
                     * The arrayref must be of type reference and must refer to an array whose components are of type byte or of type boolean.
                     * The index must be of type int. Both arrayref and index are popped from the operand stack.
                     * The byte value in the component of the array at index is retrieved, sign-extended to an int value, and pushed onto the top of the operand stack.
                     */
                    index = getPureValue(stack.pop());
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
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
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    stack.push(setIntValueType(getPureValue(checkValueType(object.getValue(index), JVMType.C, stackMethod, stackMethodPointer, op))));
                    break;
                case AASTORE:
                    /*
                     * The arrayref must be of type reference and must refer to an array whose components are of type reference.
                     * The index must be of type int and value must be of type reference. The arrayref, index, and value are popped from the operand stack.
                     * The reference value is stored as the component of the array at index.
                     */
                    value = checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op);
                    index = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    object.setValue(index, value);
                    break;
                case IASTORE:
                    /*
                     * Store into int array
                     * Both index and value must be of type int. The arrayref, index, and value are popped from the operand stack.
                     * The int value is stored as the component of the array indexed by index.
                     */
                    value = checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op);
                    index = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    object.setValue(index, value);
                    break;
                case BASTORE:
                    /*
                     * Store into byte or boolean array
                     * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                     * The int value is truncated to a byte and stored as the component of the array indexed by index.
                     */
                    value = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    index = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    JVMType type = object.getArrayType();
                    if (type == JVMType.Z || type == JVMType.B) {
                        object.setValue(index, setValueType(value, type));
                    } else {
                        throw new RuntimeException("Wrong type of array\n" + getStackTrace(stackMethod, stackMethodPointer, op, false));
                    }
                    break;
                case CASTORE:
                    /*
                     * The arrayref must be of type reference and must refer to an array whose components are of type char.
                     * The index and the value must both be of type int. The arrayref, index, and value are popped from the operand stack.
                     * The int value is truncated to a char and stored as the component of the array indexed by index.
                     */
                    value = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    index = getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, stackMethodPointer, op));
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
                    checkArrayObject(object, stackMethod, stackMethodPointer, op);
                    object.setValue(index, setCharValueType(value));
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
                    object = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, stackMethodPointer, op)));
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
                            String str = entry.getStr();
                            stack.push(setRefValueType(createStringInstance(str.toCharArray())));
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
                    System.err.println(getStackTrace(stackMethod, stackMethodPointer, op, true));
                    System.exit(1);
            }
        }
    }

    @Nonnull
    private String getStackTrace(@Nonnull Method[] stackMethod, int pointer, @Nullable Opcode opcode, boolean showMnemonics) {
        StringBuilder stackTrace = new StringBuilder();
        for (int i = pointer; i >= 0; i--) {
            Method method = stackMethod[i];
            stackTrace.append("at ")
                    .append(method.getClassName())
                    .append(".")
                    .append(method.getNameAndType())
                    .append(i == pointer && opcode != null ? " " + opcode : "")
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
        String methodName = method.getNameAndType();
        if (HASHCODE.equals(methodName)) {
            InstanceObject object1 = heap.getInstanceObject(getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, pointer, opcode)));
            stack.push(setIntValueType(Objects.hashCode(object1)));
        } else if (STRING_PRINTLN.equals(methodName)) {
            System.out.println(printString(stack, stackMethod, pointer, opcode));
        } else if (STRING_PRINT.equals(methodName)) {
            System.out.print(printString(stack, stackMethod, pointer, opcode));
        } else if (CHAR_PRINT.equals(methodName)) {
            System.out.print((char) getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, pointer, opcode)));
            stack.pop();
        } else if (CHAR_PRINTLN.equals(methodName)) {
            System.out.println((char) getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, pointer, opcode)));
            stack.pop();
        } else if (INT_PRINT.equals(methodName)) {
            System.out.print(getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, pointer, opcode)));
            stack.pop();
        } else if (INT_PRINTLN.equals(methodName)) {
            System.out.println(getPureValue(checkValueType(stack.pop(), JVMType.I, stackMethod, pointer, opcode)));
            stack.pop();
        }
    }

    @Nonnull
    private char[] printString(@Nonnull StackFrame stack, @Nonnull Method[] stackMethod, int pointer, Opcode opcode) {
        InstanceObject stringObject = heap.getInstanceObject(
                getPureValue(checkValueType(stack.pop(), JVMType.A, stackMethod, pointer, opcode)));
        InstanceObject charArrayObject = heap.getInstanceObject(getPureValue(checkValueType(
                stringObject.getValue(stringObject.getIndexByFieldName("value:[C")),
                JVMType.A,
                stackMethod,
                pointer,
                opcode)));
        stack.pop();
        char[] buf = new char[charArrayObject.size()];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) charArrayObject.getValue(i);
        }
        return buf;
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

    private long checkValueType(long value, @Nonnull JVMType type, @Nonnull Method[] stackMethod, int pointer, Opcode opcode) {
        if (getValueType(value) != type.ordinal()) {
            throw new RuntimeException("Wrong types: "
                    + JVMType.values()[getValueType(value)]
                    + " is not equal "
                    + type.name()
                    + "\n"
                    + getStackTrace(stackMethod, pointer, opcode, false));
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
            throw new RuntimeException("Object is not array\n" + getStackTrace(stackMethod, pointer, opcode, false));
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

    private long setCharValueType(long value) {
        return setValueType(JVMType.C.ordinal()) ^ value;
    }

    private long setValueType(long value, @Nonnull JVMType type) {
        return setValueType(type.ordinal()) ^ value;
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

    private int createStringInstance(@Nonnull char[] str) {
        int stringObjectRef = allocateInstanceObjectAndGetReference(STRING);
        int charArrayRef = allocateArray(JVMType.C.name(), str.length);
        InstanceObject charArrayObj = heap.getInstanceObject(charArrayRef);
        for (int i = 0; i < str.length; i++) {
            charArrayObj.setValue(i, setCharValueType(str[i]));
        }
        InstanceObject stringObj = heap.getInstanceObject(stringObjectRef);
        stringObj.setValue(0, setRefValueType(charArrayRef));
        return stringObjectRef;
    }

    private int allocateInstanceObjectAndGetReference(String sourceKlassName, int cpIndex) {
        Klass sourceKlass = heap.getKlassLoader().getLoadedKlassByName(sourceKlassName);
        String destKlassName = sourceKlass.getKlassNameByCPIndex((short) cpIndex);
        return allocateInstanceObjectAndGetReference(destKlassName);
    }

    private int allocateInstanceObjectAndGetReference(String klassName) {
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

        return heap.getObjectRef(new InstanceObject(fields,
                heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true)));
    }


    private int allocateArray(String type, int count) {
        int klassIndex = -1;
        if (type.startsWith("L")) {
            String klassName = type.substring(1, type.length() - 1);
            klassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(klassName, true);
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
        int klassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(destKlassName, true);
        return heap.getObjectRef(allocateArrayOfRef(count, klassIndex));
    }

    private InstanceObject allocateArrayOfRef(int count, int klassIndex) {
        return new InstanceObject(JVMType.A.name(), count, klassIndex);
    }


}

