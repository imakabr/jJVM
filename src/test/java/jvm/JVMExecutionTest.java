package jvm;

import jvm.engine.ExecutionEngine;
import jvm.heap.*;
import jvm.lang.KlassCastException;
import jvm.parser.Method;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;


public class JVMExecutionTest {

    @Test
    public void simpleStaticFieldsInstanceKlassTest() throws NoSuchFieldException, IllegalAccessException {
        String fName = "jvm/examples/SimpleStatic";

        Heap heap = new Heap(500, 50);
        ;
        heap.getKlassLoader().loadKlass(fName);

        InstanceKlass simpleStaticFieldsInstanceKlass = heap.getInstanceKlass(heap.getKlassLoader().getInstanceKlassIndexByName(fName, false));
        InstanceObject object = heap.getInstanceObject(simpleStaticFieldsInstanceKlass.getObjectRef());
        int fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("b:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(555, getIntValue(object.getValue(fieldValueIndex)));

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("c:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(-129, getIntValue(object.getValue(fieldValueIndex)));

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("d:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(333, getIntValue(object.getValue(fieldValueIndex)));

    }

    @Test
    public void complexStaticFieldsInstanceKlassTest() {
        String fName = "jvm/examples/ComplexStatic";
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int complexStaticFieldsInstanceKlassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(fName, false);
        InstanceKlass complexStaticFieldsInstanceKlass = heap.getInstanceKlass(complexStaticFieldsInstanceKlassIndex);
        InstanceObject object = heap.getInstanceObject(complexStaticFieldsInstanceKlass.getObjectRef());
        int fieldValueIndex = complexStaticFieldsInstanceKlass.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(888, getIntValue(object.getValue(fieldValueIndex)));
    }

    @Test
    public void simpleStaticMethodTest() {
        String fName = "jvm/examples/SimpleStatic";
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);
        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleStatic.m0:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(759, result);
    }

    @Test
    public void complexStaticMethodTest() {
        String fName = "jvm/examples/ComplexStatic";
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);
        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ComplexStatic.m:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(2057, result);
    }

    @Test
    public void simpleFieldInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m:()V");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);

        InstanceObject simpleClassObject = heap.getInstanceObject(3);

        int fieldValueIndex = simpleClassObject.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(1, getIntValue(simpleClassObject.getValue(fieldValueIndex)));

        fieldValueIndex = simpleClassObject.getIndexByFieldName("b:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(2, getIntValue(simpleClassObject.getValue(fieldValueIndex)));

        fieldValueIndex = simpleClassObject.getIndexByFieldName("c:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(3, getIntValue(simpleClassObject.getValue(fieldValueIndex)));
    }

    @Test
    public void simpleInvokeSpecialMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m2:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(140, result);
    }

    @Test
    public void simpleInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m3:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(148, result);
    }

    @Test
    public void complexInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        String fName = "jvm/examples/ComplexObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ComplexObject.m:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(86, result);
    }

    @Test
    public void complexStaticFieldInheritanceTest() {
        String fName = "jvm/examples/ChildChildStatic";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        InstanceKlass parentStaticKlass = heap.getInstanceKlass(heap.getKlassLoader().getInstanceKlassIndexByName("jvm/examples/ParentStatic", false));
        assertEquals(0, parentStaticKlass.getIndexByFieldName("a:I"));
        assertThrows(NullPointerException.class, () -> parentStaticKlass.getIndexByFieldName("b:I"));

        InstanceKlass childStaticKlass = heap.getInstanceKlass(heap.getKlassLoader().getInstanceKlassIndexByName("jvm/examples/ChildStatic", false));
        assertEquals(0, childStaticKlass.getIndexByFieldName("a:I"));
        assertEquals(1, childStaticKlass.getIndexByFieldName("b:I"));
        assertThrows(NullPointerException.class, () -> childStaticKlass.getIndexByFieldName("c:I"));

        InstanceKlass childChildStaticKlass = heap.getInstanceKlass(heap.getKlassLoader().getInstanceKlassIndexByName(fName, false));
        assertEquals(0, childChildStaticKlass.getIndexByFieldName("a:I"));
        assertEquals(1, childChildStaticKlass.getIndexByFieldName("b:I"));
        assertEquals(2, childChildStaticKlass.getIndexByFieldName("c:I"));
        assertThrows(NullPointerException.class, () -> childChildStaticKlass.getIndexByFieldName("d:I"));
    }

    @Test
    public void complexObjectFieldInheritanceTest() {
        String fName = "jvm/examples/ChildChildObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ChildChildObject.m:()V");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);

        InstanceObject object = heap.getInstanceObject(3);

        int fieldValueIndex = object.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(11, getIntValue(object.getValue(fieldValueIndex)));

    }

    @Test
    public void complexStaticMethodInheritanceTest() {
        String fName = "jvm/examples/ChildChildStatic";
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);
        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ChildChildStatic.childChildMethod:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(45, result);
    }

    @Test
    public void createIntArrayAndGetLength() {
        // check NEWARRAY, ARRAYLENGTH
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createIntArrayAndGetLength:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(13, result);
    }

    @Test
    public void createSimpleObjectArrayAndGetLength() {
        // check ANEWARRAY, ARRAYLENGTH
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createSimpleObjectArrayAndGetLength:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(36, result);
    }

    @Test
    public void simpleIntArrayPutGet() {
        // check ANEWARRAY, ARRAYLENGTH, IALOAD, IASTORE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createIntArrayAndGetValueFromIt:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(122, result);
    }

    @Test
    public void createSimpleObjectArrayAndGetValueFromIt() {
        // check ANEWARRAY, ARRAYLENGTH, AALOAD, AASTORE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createSimpleObjectArrayAndGetValueFromIt:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(2, result);
    }

    @Test
    public void createMultiIntArrayAndGetLength() {
        // check MULTIANEWARRAY, ARRAYLENGTH
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createMultiIntArrayAndGetLength:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(3, result);
    }

    @Test
    public void createMultiIntArrayAndGetValueFromIt() {
        // check MULTIANEWARRAY, AALOAD, AASTORE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createMultiIntArrayAndGetValueFromIt:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(9, result);
    }

    @Test
    public void createMultiSimpleObjectArrayAndGetNullFromIt() {
        // check MULTIANEWARRAY, AALOAD, AASTORE, ACONST_NULL, ARETURN
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createMultiSimpleObjectArrayAndGetNullFromIt:()Ljvm/examples/SimpleObject;");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void createObjectGetHashCode() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createObjectGetHashCode:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        InstanceObject object = heap.getInstanceObject(3);
        assertEquals(Objects.hashCode(object), result);
    }

    @Test
    public void createObjectGetOverriddenHashCode() {
        // check LDC
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createObjectGetOverriddenHashCode:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(-1555573285, result);
    }

    @Test
    public void checkDifferentObjectWithEqualsMethod() {
        // check IF_ACMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkDifferentObjectWithEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkSameObjectWithEqualsMethod() {
        // check IF_ACMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSameObjectWithEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkSameSimpleObjectWithEqualsMethod() {
        // check IF_ICMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSameSimpleObjectWithEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkSimpleObjectNotEqualsMethod() {
        // check IF_ICMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSimpleObjectNotEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFACMPEQTrue() {
        // check IF_ACMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFACMPEQTrue:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFACMPEQFalse() {
        // check IF_ACMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFACMPEQFalse:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFICMPEQFalse() {
        // check IF_ICMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPEQFalse:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFICMPEQTrue() {
        // check IF_ICMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPEQTrue:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFNONNULLFalse() {
        // check IFNONNULL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNONNULLFalse:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFNONNULLTrue() {
        // check IFNONNULL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNONNULLTrue:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFNULLTrue() {
        // check IFNULL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNULLTrue:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFNULLFalse() {
        // check IFNULL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNULLFalse:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkCastMethod() {
        // check CHECKCAST
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSimpleCastMethod:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkComplexCastMethod() {
        // check CHECKCAST
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkComplexCastMethod:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(11, result);
    }

    @Test
    public void checkComplexCastMethod2() {
        // check CHECKCAST
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkComplexCastMethod2:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        assertThrows("Should throw KlassCastException", KlassCastException.class, () -> new ExecutionEngine(heap).invoke(method));
    }

    @Test
    public void checkIFICMPLT() {
        // check IF_ICMPLT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPLT:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFICMPGT() {
        // check IF_ICMPGT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPGT:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFICMPGE() {
        // check IF_ICMPGE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPGE:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFICMPLE() {
        // check IF_ICMPLE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFICMPLE:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void createCharArray() {
        // check CASTORE, CALOAD
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createCharArray:()C");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(102, result);
    }

    @Test
    public void createBooleanArray() {
        // check BASTORE, BALOAD
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.createBooleanArray:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkLoopFor() {
        // check loop (IINC, GOTO, IF_ICMPGT)
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.calculateSum:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(5050, result);
    }

    @Test
    public void checkBubbleSorting() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkBubbleSorting:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkStringToCharArray() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkStringToCharArray:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkStringReplace() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkStringReplace:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkStringEqualsTrue() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkStringEqualsTrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkStringEqualsFalse() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkStringEqualsFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkStringHashCode() {
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkStringHashCode:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1923188771, result);
    }

    @Test
    public void checkIFGEFalse() {
        // check IFGE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFGEFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFGETrue() {
        // check IFGE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFGETrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFLETrue() {
        // check IFLE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFLETrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFLEFalse() {
        // check IFLE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFLEFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFLTTrue() {
        // check IFLT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFLTTrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFLTFalse() {
        // check IFLT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFLTFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFGTTrue() {
        // check IFGT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFGTTrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFGTFalse() {
        // check IFGT
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFGTFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFNETrue() {
        // check IFNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNETrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFNEFalse() {
        // check IFNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFNEFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIFEQTrue() {
        // check IFEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFEQTrue:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkIFEQFalse() {
        // check IFEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIFEQFalse:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkIMUL() {
        // check IMUL
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIMUL:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(6, result);
    }

    @Test
    public void checkINEG() {
        // check INEG
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkINEG:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(-1, result);
    }

    @Test
    public void checkISUB() {
        // check ISUB
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkISUB:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(2, result);
    }

    @Test
    public void checkIDIV() {
        // check IDIV
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIDIV:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(2, result);
    }

    @Test
    public void checkIREM() {
        // check IREM
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIREM:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(3, result);
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
