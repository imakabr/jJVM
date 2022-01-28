package jvm.garbage_collector;

import jvm.JVMType;
import jvm.engine.ExecutionEngine;
import jvm.engine.StackFrame;
import jvm.heap.Heap;
import jvm.heap.InstanceKlass;
import jvm.heap.InstanceObject;
import jvm.heap.ReferenceTable;
import jvm.parser.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static jvm.engine.ExecutionEngine.NULL;

public class MarkAndSweep implements GarbageCollector {

    @Nonnull
    private final StackFrame stackFrame;
    @Nullable
    private Heap heap;
    private int count;
    private boolean inProgress;

    public MarkAndSweep(@Nonnull StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    @Override
    public void setHeap(@Nonnull Heap heap) {
        this.heap = heap;
    }

    @Override
    public void run() {
        if (heap != null) {
            inProgress = true;
            HashSet<Integer> aliveObjects = new HashSet<>();
            LinkedList<Integer> queue = new LinkedList<>();
            for (InstanceKlass klass : heap.getInstanceKlasses()) {
                if (klass == null) {
                    break;
                }
                int objectRef = klass.getObjectRef();
                if (objectRef != -1) {
                    queue.add(objectRef);
                }
            }
            findAliveObjects(queue, aliveObjects);

            long[] stack = stackFrame.getStack();
            int stackSize = stackFrame.getSize();
            for (int i = 0; i <= stackSize; i++) {
                long value = stack[i];
                if (getValueType(value) == JVMType.A.ordinal() && getPureValue(value) != NULL) {
                    queue.add(getPureValue(value));
                }
            }

            findAliveObjects(queue, aliveObjects);

            long startTime = System.currentTimeMillis();
            InstanceObject[] objects = heap.getInstanceObjects();
            ReferenceTable refTable = heap.getReferenceTable();
            for (int objRef = 0; objRef < objects.length; objRef++) {
                int objIndex = refTable.getInstanceObjectIndex(objRef);
                if (objIndex != -1 && objects[objIndex] != null && !aliveObjects.contains(objRef)) {
                    InstanceObject object = objects[objIndex];
                    int klassIndex = object.getKlassIndex();
                    if (klassIndex != -1) {
                        InstanceKlass klass = heap.getInstanceKlass(klassIndex);
                        int virtualMethodIndex = klass.getIndexByVirtualMethodName("finalize:()V");
                        int methodIndex = klass.getMethodIndex(virtualMethodIndex);
                        Method method = heap.getMethodRepo().getMethod(methodIndex);
                        StackFrame stackFrame = new StackFrame(method.getVarSize(), method.getOperandSize());
                        stackFrame.setLocalVar(0, setRefValueType(objRef));
                        new ExecutionEngine(heap, stackFrame).invoke(method);
                    }
                    objects[objIndex] = null;
                    heap.decrementInstanceObjectSize();
                    refTable.clearObjectIndex(objRef);
                }
                if (System.currentTimeMillis() - startTime > 400) {
                    break;
                }
            }
            System.out.println("GC done " + count++);
            inProgress = false;
        }
    }

    @Override
    public boolean isInProgress() {
        return inProgress;
    }

    private void findAliveObjects(@Nonnull LinkedList<Integer> queue, @Nonnull HashSet<Integer> visitedObjects) {
        if (heap != null) {
            ReferenceTable refTable = heap.getReferenceTable();
            while (!queue.isEmpty()) {
                int objectRef = queue.pop();
                if (!visitedObjects.contains(objectRef)) {
                    setRefValueType(objectRef);
                    stackFrame.getSize();
                    int objectIndex = refTable.getInstanceObjectIndex(objectRef);
                    if (objectIndex != -1) {
                        InstanceObject object = heap.getInstanceObject(objectRef);
                        for (long value : object.getFieldValues()) {
                            if (getValueType(value) == JVMType.A.ordinal() && getPureValue(value) != NULL) {
                                queue.add(getPureValue(value));
                            }
                        }
                        visitedObjects.add(objectRef);
                    }
                }
            }
        }
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    private int getPureValue(long value) {
        return (int) value;
    }

    private long setRefValueType(int value) {
        return setValueType(JVMType.A.ordinal()) ^ value;
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

}
