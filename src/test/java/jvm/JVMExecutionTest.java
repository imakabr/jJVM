package jvm;

import jvm.engine.ExecutionEngine;
import jvm.heap.*;
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
        assertEquals(2569, result);
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

        InstanceObject simpleClassObject = heap.getInstanceObject(2);

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

        InstanceObject object = heap.getInstanceObject(2);

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
        InstanceObject object = heap.getInstanceObject(2);
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
    public void checkDifferentSimpleObjectWithEqualsMethod() {
        // check IF_ACMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkDifferentSimpleObjectWithEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkSameSimpleObjectWithEqualsMethod() {
        // check IF_ACMPNE
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSameSimpleObjectWithEqualsMethod:()Z");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkDifferentObjectsAreNotEqual() {
        // check IF_ACMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkDifferentObjectsAreNotEqual:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(0, result);
    }

    @Test
    public void checkSameObjectsAreEqual() {
        // check IF_ACMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSameObjectsAreEqual:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkSameIntAreEqual() {
        // check IF_ICMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkSameIntAreEqual:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
    }

    @Test
    public void checkSameIntAreNotEqual() {
        // check IF_ICMPEQ
        String fName = "jvm/examples/SimpleObject";

        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.checkIntAreNotEqual:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1, result);
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
