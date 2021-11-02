package jvm.engine;


import jvm.JVMType;
import jvm.JVMValue;
import jvm.heap.*;
import jvm.parser.Method;
import jvm.parser.Klass;

import java.util.ArrayList;
import java.util.List;

import static jvm.engine.Opcode.*;
import static jvm.heap.KlassLoader.JAVA_LANG_OBJECT;

public final class ExecutionEngine {

    private final Opcode[] table = new Opcode[256];
    private final Heap heap;

    public ExecutionEngine(Heap heap) {
        this.heap = heap;
        for (Opcode op : values()) {
            table[op.getOpcode()] = op;
        }
    }

    public JVMValue invoke(Method firstMethod) {

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
                    stack.push(0);
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
                    method = stackMethod[--stackMethodPointer];
                    stackMethod[stackMethodPointer + 1] = null;
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stack.destroyCurrentMethodStack(method.getVarSize(), method.getOperandSize(), true);
                    programCounter = stack.programCounter;
                    break;
                case AASTORE:
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
                    stack.push((int) byteCode[programCounter++]);
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
                    object = heap.getInstanceObject((int) stack.pop());
                    fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                    stack.push(object.getValue(fieldValueIndex).value);
                    break;
                case GETSTATIC:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    //---------------------------------------------------------------------------------

                    // todo restore indexes for resolving
                    objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                    fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));

                    //---------------------------------------------------------------------------------
                    stack.push(heap.getInstanceObject(objectRef).getValue(fieldValueIndex).value); // to do deal with type to JVMValue
                    break;
                //----------------------------------------------------------------------------------------------------------------------
                case GOTO:
                    programCounter = ((int) byteCode[programCounter] << 8) + (int) byteCode[programCounter + 1];
                    break;
                case I2D:
                    throw new IllegalArgumentException("convert int to double");
//                    break;
                case IADD:
                    stack.push(stack.pop() + stack.pop());
                    break;
                case IAND:
                    stack.push(stack.pop() & stack.pop());
                    break;
                case ICONST_0:
                    stack.push(0);
                    break;
                case ICONST_1:
                    stack.push(1);
                    break;
                case ICONST_2:
                    stack.push(2);
                    break;
                case ICONST_3:
                    stack.push(3);
                    break;
                case ICONST_4:
                    stack.push(4);
                    break;
                case ICONST_5:
                    stack.push(5);
                    break;
                case ICONST_M1:
                    stack.push(-1);
                    break;
                case IDIV:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    if (second == 0) throw new ArithmeticException("cannot divide 0");
                    stack.push(second / first);
                    break;
                case IF_ICMPEQ:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    jumpTo = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    if (first == second) {
                        programCounter = jumpTo; // The -1 is necessary as we've already inc'd programCounter
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
                    if (methodIndex == -1) {
                        stack.pop();
                        break; // todo implement Object class
                    }
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
                    int klassIndex = heap.getInstanceObject((int) stack.getObjectRefBeforeInvoke(getArgSize(klassName, cpLookup))).getKlassIndex();
                    int virtualMethodIndex = getVirtualMethodIndex(klassName, cpLookup, klassIndex); // todo restore to resolution
                    methodIndex = heap.getInstanceKlass(klassIndex).getMethodIndex(virtualMethodIndex);

                    method = heap.getMethodRepo().getMethod(methodIndex);
                    byteCode = method.getBytecode();
                    klassName = method.getClassName();
                    stackMethod[++stackMethodPointer] = method;
                    stack.programCounter = programCounter;
                    programCounter = 0;
                    stack.initNewMethodStack(method.getArgSize() + 1, method.getVarSize(), method.getOperandSize());
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
                        int returnValue = (int) stack.pop();
                        return JVMValue.entry(returnValue);
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
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push(first - second);
                    break;

                //--------------------------------------------------------------------------------------------------------------------------------------
                case MONITORENTER:
                case MONITOREXIT:
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case NEW:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    stack.push(allocateInstanceObjectAndGetReference(klassName, cpLookup));
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
                    object = heap.getInstanceObject((int) stack.pop());
                    fieldValueIndex = object.getIndexByFieldName(getFieldName(getKlassFieldName(klassName, cpLookup))); //todo restore index for resolving
                    object.setValue(fieldValueIndex, new JVMValue(JVMType.I, value));
                    break;
                case PUTSTATIC:
                    cpLookup = ((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++];
                    //---------------------------------------------------------------------------------
                    // todo restore indexes for resolving
                    objectRef = heap.getInstanceKlass(getInstanceKlassIndex(getKlassFieldName(klassName, cpLookup))).getObjectRef();
                    fieldValueIndex = getStaticFieldIndex(getKlassFieldName(klassName, cpLookup));
                    //----------------------------------------------------------------------------------
                    heap.getInstanceObject(objectRef)
                            .setValue(fieldValueIndex, new JVMValue(JVMType.I, stack.pop())); // to do create type to JVMValue
                    break;
                //--------------------------------------------------------------------------------------------------------------------------------------
                case RET:
                    throw new IllegalArgumentException("Illegal opcode byte: " + (b & 0xff) + " encountered at position " + (programCounter - 1) + ". Stopping.");
                case RETURN:
                    if (stackMethodPointer == 0) {
                        return null;
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
                    stack.push(((int) byteCode[programCounter++] << 8) + (int) byteCode[programCounter++]);
                    break;
                case SWAP:
                    first = (int) stack.pop();
                    second = (int) stack.pop();
                    stack.push(first);
                    stack.push(second);
                    break;
                //-------------------------------------------------------------------------------------------------------------------------------------
                case LDC:
                    System.out.print("Executing " + op + " with param bytes: ");
                    for (int i = programCounter; i < programCounter + num; i++) {
                        System.out.print(byteCode[i] + " ");
                    }
                    programCounter += num;
                    System.out.println();
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
        if ("java/lang/Object.<init>:()V".equals(methodName)) {
            return -1;
        }
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
        while(!JAVA_LANG_OBJECT.equals(current.getParent())) {
            klasses.add(heap.getKlassLoader().getLoadedKlassByName(current.getKlassName()));
            current = heap.getKlassLoader().getLoadedKlassByName(current.getParent());
        }
        klasses.add(heap.getKlassLoader().getLoadedKlassByName(current.getKlassName()));
        List<String> fields = new ArrayList<>();
        for (int i = klasses.size() - 1; i >= 0; i--) {
            fields.addAll(klasses.get(i).getObjectFieldNames());
        }

        return heap.getObjectRef(new InstanceObject(fields,
                heap.getKlassLoader().getInstanceKlassIndexByName(destKlassName, false)));
    }

}

