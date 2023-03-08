package jvm;

import jvm.heap.api.Heap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.lang.ClassCastExceptionJVM;
import jvm.lang.NullPointerExceptionJVM;
import jvm.lang.OutOfMemoryErrorJVM;
import jvm.lang.RuntimeExceptionJVM;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.junit.Assert.*;


public class JVMExecutionTest {

    public static final int FIRST_NOT_SYSTEM_INSTANCE = 11;
    public static final String INSTRUCTION = "jvm/examples/InstructionExample";
    public static final String ARRAY = "jvm/examples/ArrayExample";
    public static final String ALGORITHM = "jvm/examples/AlgorithmExample";
    public static final String SIMPLE_OBJECT = "jvm/examples/SimpleObject";

    @Test
    public void simpleStaticFieldsInstanceKlassTest() {
        String fName = "jvm/examples/SimpleStatic";

        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        InstanceKlass simpleStaticFieldsInstanceKlass = heap.getInstanceKlass(
                Objects.requireNonNull(heap.getKlassLoader().getInstanceKlassIndexByName(fName, false)));
        InstanceObject object = heap.getInstanceObject(simpleStaticFieldsInstanceKlass.getObjectRef());
        int fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("b:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(555, getIntValue(object.getFieldValue(fieldValueIndex)));

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("c:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(-129, getIntValue(object.getFieldValue(fieldValueIndex)));

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("d:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(333, getIntValue(object.getFieldValue(fieldValueIndex)));

    }

    @Test
    public void complexStaticFieldsInstanceKlassTest() {
        String fName = "jvm/examples/ComplexStatic";
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        int complexStaticFieldsInstanceKlassIndex = Objects.requireNonNull(
                heap.getKlassLoader().getInstanceKlassIndexByName(fName, false));
        InstanceKlass complexStaticFieldsInstanceKlass = heap.getInstanceKlass(complexStaticFieldsInstanceKlassIndex);
        InstanceObject object = heap.getInstanceObject(complexStaticFieldsInstanceKlass.getObjectRef());
        int fieldValueIndex = complexStaticFieldsInstanceKlass.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(888, getIntValue(object.getFieldValue(fieldValueIndex)));
    }

    @Test
    public void simpleStaticMethodTest() {
        String fName = "jvm/examples/SimpleStatic";
        checkMethod(fName, "m0:()I", 759);
    }

    @Test
    public void complexStaticMethodTest() {
        String fName = "jvm/examples/ComplexStatic";
        checkMethod(fName, "m:()I", 2057);
    }

    @Test
    public void complexStaticBMethodTest() {
        String fName = "jvm/examples/StaticB";
        checkMethod(fName, "test:()I", 15);
    }

    @Test
    public void complexStaticCMethodTest() {
        String fName = "jvm/examples/StaticC";
        checkMethod(fName, "test:()I", 48);
    }

    @Test
    public void simpleFieldInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD
        String fName = "jvm/examples/SimpleObject";

        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m:()V");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        virtualMachine.getEngine().invoke(method);

        InstanceObject simpleClassObject = heap.getInstanceObject(FIRST_NOT_SYSTEM_INSTANCE);

        int fieldValueIndex = simpleClassObject.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(1, getIntValue(simpleClassObject.getFieldValue(fieldValueIndex)));

        fieldValueIndex = simpleClassObject.getIndexByFieldName("b:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(2, getIntValue(simpleClassObject.getFieldValue(fieldValueIndex)));

        fieldValueIndex = simpleClassObject.getIndexByFieldName("c:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(3, getIntValue(simpleClassObject.getFieldValue(fieldValueIndex)));
    }

    @Test
    public void simpleInvokeSpecialMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD
        checkMethodInSimpleObjectClass("m2:()I", 140);
    }

    @Test
    public void simpleInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        checkMethodInSimpleObjectClass("m3:()I", 148);
    }

    @Test
    public void complexInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        String fName = "jvm/examples/ComplexObject";
        checkMethod(fName, "m:()I", 86);
    }

    @Test
    public void complexStaticFieldInheritanceTest() {
        String fName = "jvm/examples/ChildChildStatic";

        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        InstanceKlass parentStaticKlass = heap.getInstanceKlass(Objects.requireNonNull(
                heap.getKlassLoader().getInstanceKlassIndexByName("jvm/examples/ParentStatic", false)));
        assertEquals(0, parentStaticKlass.getIndexByFieldName("a:I"));
        assertThrows(NullPointerException.class, () -> parentStaticKlass.getIndexByFieldName("b:I"));

        InstanceKlass childStaticKlass = heap.getInstanceKlass(Objects.requireNonNull(
                heap.getKlassLoader().getInstanceKlassIndexByName("jvm/examples/ChildStatic", false)));
        assertEquals(0, childStaticKlass.getIndexByFieldName("a:I"));
        assertEquals(1, childStaticKlass.getIndexByFieldName("b:I"));
        assertThrows(NullPointerException.class, () -> childStaticKlass.getIndexByFieldName("c:I"));

        InstanceKlass childChildStaticKlass = heap.getInstanceKlass(Objects.requireNonNull(
                heap.getKlassLoader().getInstanceKlassIndexByName(fName, false)));
        assertEquals(0, childChildStaticKlass.getIndexByFieldName("a:I"));
        assertEquals(1, childChildStaticKlass.getIndexByFieldName("b:I"));
        assertEquals(2, childChildStaticKlass.getIndexByFieldName("c:I"));
        assertThrows(NullPointerException.class, () -> childChildStaticKlass.getIndexByFieldName("d:I"));
    }

    @Test
    public void complexObjectFieldInheritanceTest() {
        String fName = "jvm/examples/ChildChildObject";

        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ChildChildObject.m:()V");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        virtualMachine.getEngine().invoke(method);

        InstanceObject object = heap.getInstanceObject(FIRST_NOT_SYSTEM_INSTANCE);

        int fieldValueIndex = object.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(11, getIntValue(object.getFieldValue(fieldValueIndex)));

    }

    @Test
    public void complexStaticMethodInheritanceTest() {
        String fName = "jvm/examples/ChildChildStatic";
        checkMethod(fName, "childChildMethod:()I", 45);
    }

    @Test
    public void createIntArrayAndGetLength() {
        // check NEWARRAY, ARRAYLENGTH
        checkMethodInArrayClass("createIntArrayAndGetLength:()I", 13);
    }

    @Test
    public void createSimpleObjectArrayAndGetLength() {
        // check ANEWARRAY, ARRAYLENGTH
        checkMethodInArrayClass("createSimpleObjectArrayAndGetLength:()I", 36);
    }

    @Test
    public void simpleIntArrayPutGet() {
        // check ANEWARRAY, ARRAYLENGTH, IALOAD, IASTORE
        checkMethodInArrayClass("createIntArrayAndGetValueFromIt:()I", 122);
    }

    @Test
    public void createSimpleObjectArrayAndGetValueFromIt() {
        // check ANEWARRAY, ARRAYLENGTH, AALOAD, AASTORE
        checkMethodInArrayClass("createSimpleObjectArrayAndGetValueFromIt:()I", 2);
    }

    @Test
    public void createMultiIntArrayAndGetLength() {
        // check MULTIANEWARRAY, ARRAYLENGTH
        checkMethodInArrayClass("createMultiIntArrayAndGetLength:()I", 3);
    }

    @Test
    public void createMultiIntArrayAndGetValueFromIt() {
        // check MULTIANEWARRAY, AALOAD, AASTORE
        checkMethodInArrayClass("createMultiIntArrayAndGetValueFromIt:()I", 9);
    }

    @Test
    public void createMultiSimpleObjectArrayAndGetNullFromIt() {
        // check MULTIANEWARRAY, AALOAD, AASTORE, ACONST_NULL, ARETURN
        checkMethodInArrayClass("createMultiSimpleObjectArrayAndGetNullFromIt:()Ljvm/examples/SimpleObject;", 0);
    }

    @Test
    public void createObjectGetHashCode() {
        String fName = "jvm/examples/SimpleObject";

        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createObjectGetHashCode:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = virtualMachine.getEngine().invoke(method);
        InstanceObject object = heap.getInstanceObject(FIRST_NOT_SYSTEM_INSTANCE);
        assertEquals(Objects.hashCode(object), result);
    }

    @Test
    public void createObjectGetOverriddenHashCode() {
        // check LDC
        checkMethodInSimpleObjectClass("createObjectGetOverriddenHashCode:()I", -1555573285);
    }

    @Test
    public void checkDifferentObjectWithEqualsMethod() {
        // check IF_ACMPNE
        checkMethodInInstructionClass("checkDifferentObjectWithEqualsMethod:()Z", 0);
    }

    @Test
    public void checkSameObjectWithEqualsMethod() {
        // check IF_ACMPNE
        checkMethodInInstructionClass("checkSameObjectWithEqualsMethod:()Z", 1);
    }

    @Test
    public void checkSameSimpleObjectWithEqualsMethod() {
        // check IF_ICMPNE
        checkMethodInInstructionClass("checkSameSimpleObjectWithEqualsMethod:()Z", 1);
    }

    @Test
    public void checkSimpleObjectNotEqualsMethod() {
        // check IF_ICMPNE
        checkMethodInInstructionClass("checkSimpleObjectNotEqualsMethod:()Z", 0);
    }

    @Test
    public void checkIFACMPEQTrue() {
        // check IF_ACMPEQ
        checkMethodInInstructionClass("checkIFACMPEQTrue:()I", 0);
    }

    @Test
    public void checkIFACMPEQFalse() {
        // check IF_ACMPEQ
        checkMethodInInstructionClass("checkIFACMPEQFalse:()I", 1);
    }

    @Test
    public void checkIFICMPEQFalse() {
        // check IF_ICMPEQ
        checkMethodInInstructionClass("checkIFICMPEQFalse:()I", 1);
    }

    @Test
    public void checkIFICMPEQTrue() {
        // check IF_ICMPEQ
        checkMethodInInstructionClass("checkIFICMPEQTrue:()I", 1);
    }

    @Test
    public void checkIFNONNULLFalse() {
        // check IFNONNULL
        checkMethodInInstructionClass("checkIFNONNULLFalse:()I", 0);
    }

    @Test
    public void checkIFNONNULLTrue() {
        // check IFNONNULL
        checkMethodInInstructionClass("checkIFNONNULLTrue:()I", 1);
    }

    @Test
    public void checkIFNULLTrue() {
        // check IFNULL
        checkMethodInInstructionClass("checkIFNULLTrue:()I", 1);
    }

    @Test
    public void checkIFNULLFalse() {
        // check IFNULL
        checkMethodInInstructionClass("checkIFNULLFalse:()I", 0);
    }

    @Test
    public void checkCastMethod() {
        // check CHECKCAST
        checkMethodInInstructionClass("checkSimpleCastMethod:()I", 1);
    }

    @Test
    public void checkNullCastMethod() {
        // check CHECKCAST
        checkMethodInInstructionClass("checkNullCastMethod:()Z", 1);
    }

    @Test
    public void checkComplexCastMethod() {
        // check CHECKCAST
        checkMethodInInstructionClass("checkComplexCastMethod:()I", 11);
    }

    @Test
    public void checkIFICMPLT() {
        // check IF_ICMPLT
        checkMethodInInstructionClass("checkIFICMPLT:()I", 1);
    }

    @Test
    public void checkIFICMPGT() {
        // check IF_ICMPGT
        checkMethodInInstructionClass("checkIFICMPGT:()I", 0);
    }

    @Test
    public void checkIFICMPGE() {
        // check IF_ICMPGE
        checkMethodInInstructionClass("checkIFICMPGE:()I", 1);
    }

    @Test
    public void checkIFICMPLE() {
        // check IF_ICMPLE
        checkMethodInInstructionClass("checkIFICMPLE:()I", 0);
    }

    @Test
    public void createCharArray() {
        // check CASTORE, CALOAD
        checkMethodInArrayClass("createCharArray:()C", 102);
    }

    @Test
    public void createBooleanArray() {
        // check BASTORE, BALOAD
        checkMethodInArrayClass("createBooleanArray:()Z", 1);
    }

    @Test
    public void checkLoopFor() {
        // check loop (IINC, GOTO, IF_ICMPGT)
        checkMethodInInstructionClass("calculateSum:()I", 5050);
    }

    @Test
    public void checkBubbleSorting() {
        checkMethodInAlgorithmClass("checkBubbleSorting:()Z", 1);
    }

    @Test
    public void checkIFGEFalse() {
        // check IFGE
        checkMethodInInstructionClass("checkIFGEFalse:()Z", 0);
    }

    @Test
    public void checkIFGETrue() {
        // check IFGE
        checkMethodInInstructionClass("checkIFGETrue:()Z", 1);
    }

    @Test
    public void checkIFLETrue() {
        // check IFLE
        checkMethodInInstructionClass("checkIFLETrue:()Z", 1);
    }

    @Test
    public void checkIFLEFalse() {
        // check IFLE
        checkMethodInInstructionClass("checkIFLEFalse:()Z", 0);
    }

    @Test
    public void checkIFLTTrue() {
        // check IFLT
        checkMethodInInstructionClass("checkIFLTTrue:()Z", 1);
    }

    @Test
    public void checkIFLTFalse() {
        // check IFLT
        checkMethodInInstructionClass("checkIFLTFalse:()Z", 0);
    }

    @Test
    public void checkIFGTTrue() {
        // check IFGT
        checkMethodInInstructionClass("checkIFGTTrue:()Z", 1);
    }

    @Test
    public void checkIFGTFalse() {
        // check IFGT
        checkMethodInInstructionClass("checkIFGTFalse:()Z", 0);
    }

    @Test
    public void checkIFNETrue() {
        // check IFNE
        checkMethodInInstructionClass("checkIFNETrue:()Z", 1);
    }

    @Test
    public void checkIFNEFalse() {
        // check IFNE
        checkMethodInInstructionClass("checkIFNEFalse:()Z", 0);
    }

    @Test
    public void checkIFEQTrue() {
        // check IFEQ
        checkMethodInInstructionClass("checkIFEQTrue:()Z", 1);
    }

    @Test
    public void checkIFEQFalse() {
        // check IFEQ
        checkMethodInInstructionClass("checkIFEQFalse:()Z", 0);
    }

    @Test
    public void checkIMUL() {
        // check IMUL
        checkMethodInInstructionClass("checkIMUL:()I", 6);
    }

    @Test
    public void checkINEG() {
        // check INEG
        checkMethodInInstructionClass("checkINEG:()I", -1);
    }

    @Test
    public void checkISUB() {
        // check ISUB
        checkMethodInInstructionClass("checkISUB:()I", 2);
    }

    @Test
    public void checkIDIV() {
        // check IDIV
        checkMethodInInstructionClass("checkIDIV:()I", 2);
    }

    @Test
    public void checkIREM() {
        // check IREM
        checkMethodInInstructionClass("checkIREM:()I", 3);
    }

    @Test
    public void checkISHL() {
        // check checkISHL
        checkMethodInInstructionClass("checkISHL:()I", 128);
    }

    @Test
    public void checkISHR() {
        // check ISHR
        checkMethodInInstructionClass("checkISHR:()I", 32);
    }

    @Test
    public void checkIUSHR() {
        // check IUSHR
        checkMethodInInstructionClass("checkIUSHR:()I", 1);
    }

    @Test
    public void checkIXOR() {
        // check IXOR
        checkMethodInInstructionClass("checkIXOR:()I", -129);
    }

    @Test
    public void checkClassCastException() {
        // check CHECKCAST
        checkException(".checkComplexCastMethod2:()I", ClassCastExceptionJVM.class, "Should throw ClassCastExceptionJVM");
    }

    @Test
    public void checkNPEWithINVOKEVIRTUAL() {
        // check NPE with INVOKEVIRTUAL
        checkException(".checkNPEWithIV:()V", NullPointerExceptionJVM.class, "Should throw NullPointerExceptionJVM");
    }

    @Test
    public void checkNPEWhithGetField() {
        // check NPE with GETFIELD
        checkException(".checkNPEWhithGetField:()V", NullPointerExceptionJVM.class, "Should throw NullPointerExceptionJVM");
    }

    @Test
    public void checkNPEWhithPutField() {
        // check NPE with PUTFIELD
        checkException(".checkNPEWhithPutField:()V", NullPointerExceptionJVM.class, "Should throw NullPointerExceptionJVM");
    }

    @Test
    public void checkOutOfMemoryError() {
        // check OutOfMemoryError - Java heap space
        checkException(".checkOutOfMemoryError:()V", OutOfMemoryErrorJVM.class, "Should throw OutOfMemoryError");
    }

    @Test
    public void checkINSTANCEOF() {
        // check INSTANCEOF
        checkMethodInInstructionClass("checkINSTANCEOF:()I", 1);
    }

    @Test
    public void checkINSTANCEOFWithNull() {
        // check INSTANCEOF
        checkMethodInInstructionClass("checkINSTANCEOFWithNull:()I", 0);
    }

    @Test
    public void checkINSTANCEOFWithInheritedClass() {
        // check INSTANCEOF
        checkMethodInInstructionClass("checkINSTANCEOFWithInheritedClass:()I", 1);
    }

    @Test
    public void checkCheckCastArray() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastArray:()I", 1);
    }

    @Test
    public void checkCheckCastMultiArray() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray:()I", 1);
    }

    @Test
    public void checkCheckCastMultiArray2() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray2:()I", 1);
    }

    @Test
    public void checkCheckCastMultiArray3() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray3:()I", 1);
    }

    @Test
    public void checkCheckCastMultiArray4() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray4:()I", 0);
    }

    @Test
    public void checkCheckCastMultiArray5() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray5:()I", 1);
    }

    @Test
    public void checkCheckCastMultiArray6() {
        // check INSTANCEOF CHECKCAST
        checkMethodInInstructionClass("checkCastMultiArray6:()I", 1);
    }

    private void checkException(@Nonnull String methodName, @Nonnull Class<? extends RuntimeExceptionJVM> klass, @Nonnull String message) {
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(INSTRUCTION);

        int methodIndex = heap.getMethodRepo().getIndexByName(INSTRUCTION + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        virtualMachine.getEngine().setExceptionDebugMode(true);
        assertThrows(message, klass, () -> virtualMachine.getEngine().invoke(method));
    }

    private void checkMethodInInstructionClass(@Nonnull String methodName, long expected) {
        checkMethod(INSTRUCTION, methodName, expected);
    }

    private void checkMethodInArrayClass(@Nonnull String methodName, long expected) {
        checkMethod(ARRAY, methodName, expected);
    }

    private void checkMethodInAlgorithmClass(@Nonnull String methodName, long expected) {
        checkMethod(ALGORITHM, methodName, expected);
    }

    private void checkMethodInSimpleObjectClass(@Nonnull String methodName, long expected) {
        checkMethod(SIMPLE_OBJECT, methodName, expected);
    }

    private void checkMethod(@Nonnull String className, @Nonnull String methodName, long expected) {
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(className);
        int methodIndex = heap.getMethodRepo().getIndexByName(className + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
        assertEquals(expected, actual);
    }

    private int getIntValue(long value) {
        int type = (int) (value >> 32);
        type = type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
        if (type != JVMType.I.ordinal()) {
            throw new RuntimeException("Value type is not int");
        }
        return (int) value;
    }

}
