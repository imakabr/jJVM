package jvm;

import jvm.engine.ExecutionEngine;
import jvm.JVMValue;
import jvm.heap.*;
import jvm.parser.KlassParser;
import jvm.parser.Method;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class JVMExecutionTest {

    private KlassParser ce;
    private byte[] buf;

    @Before
    public void initHeap() throws IllegalAccessException, NoSuchFieldException {

    }

    @Test
    public void simpleStaticFieldsInstanceKlassTest() throws NoSuchFieldException, IllegalAccessException {
        String fName = "jvm/examples/SimpleStatic";

        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        InstanceKlass simpleStaticFieldsInstanceKlass = heap.getInstanceKlass(0);
        int fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("b:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(555, simpleStaticFieldsInstanceKlass.getValue(fieldValueIndex).value);

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("c:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(127, simpleStaticFieldsInstanceKlass.getValue(fieldValueIndex).value);

        fieldValueIndex = simpleStaticFieldsInstanceKlass.getIndexByFieldName("d:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(333, simpleStaticFieldsInstanceKlass.getValue(fieldValueIndex).value);

    }

    @Test
    public void complexStaticFieldsInstanceKlassTest()  {
        String fName = "jvm/examples/ComplexStatic";
        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        int complexStaticFieldsInstanceKlassIndex = heap.getKlassLoader().getInstanceKlassIndexByName(fName);
        InstanceKlass complexStaticFieldsInstanceKlass = heap.getInstanceKlass(complexStaticFieldsInstanceKlassIndex);
        int fieldValueIndex = complexStaticFieldsInstanceKlass.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(888, complexStaticFieldsInstanceKlass.getValue(fieldValueIndex).value);
    }

    @Test
    public void simpleStaticMethodTest() {
        String fName = "jvm/examples/SimpleStatic";
        Heap heap = HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);
        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleStatic.m0:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);
        assertEquals(1015, result.value);
    }

    @Test
    public void complexStaticMethodTest() {
        String fName = "jvm/examples/ComplexStatic";
        Heap heap = HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);
        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ComplexStatic.m:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);
        assertEquals(2569, result.value);
    }

    @Test
    public void simpleFieldInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD
        String fName = "jvm/examples/SimpleObject";

        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m:()V");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);

        InstanceObject simpleClassObject = heap.getInstanceObject(0);

        int fieldValueIndex = simpleClassObject.getIndexByFieldName("a:I");
        assertEquals(0, fieldValueIndex);
        assertEquals(1, simpleClassObject.getValue(fieldValueIndex).value);

        fieldValueIndex = simpleClassObject.getIndexByFieldName("b:I");
        assertEquals(1, fieldValueIndex);
        assertEquals(2, simpleClassObject.getValue(fieldValueIndex).value);

        fieldValueIndex = simpleClassObject.getIndexByFieldName("c:I");
        assertEquals(2, fieldValueIndex);
        assertEquals(3, simpleClassObject.getValue(fieldValueIndex).value);
    }

    @Test
    public void simpleInvokeSpecialMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD
        String fName = "jvm/examples/SimpleObject";

        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m2:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);
        assertEquals(140, result.value);
    }

    @Test
    public void simpleInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        String fName = "jvm/examples/SimpleObject";

        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/SimpleObject.m3:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);
        assertEquals(148, result.value);
    }

    @Test
    public void complexInvokeVirtualMethodInstanceObjectTest() {
        // check NEW, INVOKESPECIAL, PUTFIELD, GETFIELD, INVOKEVIRTUAL
        String fName = "jvm/examples/ComplexObject";

        Heap heap= HeapHolder.getHeap();
        heap.getKlassLoader().loadKlass(fName);

        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/examples/ComplexObject.m:()I");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        JVMValue result = new ExecutionEngine(heap).invoke(method);
        assertEquals(86, result.value);
    }

//    @Test
//    public void complexStaticInheritanceTest() {
//        String fName = "jvm/Near";
//
//        Heap heap= HeapHolder.getHeap();
//        heap.getKlassLoader().loadKlass(fName);
//
//        int methodIndex = heap.getMethodRepo().getIndexByName("jvm/Far.m:()V");
//        Method method = heap.getMethodRepo().getMethod(methodIndex);
//        JVMValue result = new ExecutionEngine().execMethod(method);
//        assertEquals(86, result.value);
//    }


}
