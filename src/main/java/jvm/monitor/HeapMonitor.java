package jvm.monitor;

import jvm.JVMType;
import jvm.Utils;
import jvm.heap.Heap;
import jvm.heap.InstanceKlass;
import jvm.heap.InstanceObject;
import jvm.heap.ReferenceTable;
import processing.core.PApplet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class HeapMonitor {

    @Nonnull
    private final Heap heap;
    @Nonnull
    private final Set<String> classNames;
    @Nonnull
    public static final BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);


    public HeapMonitor(@Nonnull Heap heap, @Nonnull Set<String> classNames) {
        this.heap = heap;
        this.classNames = classNames;
    }

    public void run() {
        new Thread(() -> {
            new Thread(() -> PApplet.main("jvm.monitor.Sketch")).start();
            InstanceObject[] objects = heap.getInstanceObjects();
            InstanceKlass[] klasses = heap.getInstanceKlasses();
            ReferenceTable refTable = heap.getReferenceTable();
            for (; ; ) {
                Map<String, Integer> instanceKlasses = new HashMap<>(); // class name -> instance klass index
                Map<Integer, Integer> objectCounts = new HashMap<>(); // instance klass index -> count
                int klassesSize = heap.getInstanceKlassSize();
                for (int klassIndex = 0; klassIndex < klassesSize; klassIndex++) {
                    InstanceKlass klass = klasses[klassIndex];
                    if (klass != null && classNames.contains(klass.getName())) {
                        int finalKlassIndex = klassIndex;
                        instanceKlasses.computeIfAbsent(klass.getName(), key -> finalKlassIndex);
                        objectCounts.computeIfAbsent(klassIndex, key -> 0);
                    }
                }
                Set<Integer> innerObjects = new HashSet<>();
                int objectSize = heap.getInstanceObjectSize();
                for (int i = 0; i < objectSize; i++) {
                    InstanceObject object = objects[i];
                    if (object != null && objectCounts.containsKey(object.getKlassIndex())) {
                        int count = 1;
                        for (int j = 0; j < object.size(); j++) {
                            //---------------------------------------------------------
                            // todo type checking with indexByFieldName. if the type is an array (other than an array of references),
                            //  it should increment the counter and add the reference to innerObjects
                            long value = object.getValue(j);
                            if (Utils.getValueType(value) == JVMType.A.ordinal()) {
                                int objRef = Utils.getPureValue(value);
                                int objIndex = refTable.getInstanceObjectIndex(objRef);
                                InstanceObject innerObj = objIndex != -1 ? objects[objIndex] : null;
                                if (innerObj != null && innerObj.isArray() && !innerObjects.contains(objRef)) {
                                    if (innerObj.size() == 0) {
                                        count++;
                                        innerObjects.add(objRef);
                                    } else if (innerObj.size() != 0 // check that the inner array contains no references
                                            && Utils.getValueType(innerObj.getValue(0)) != JVMType.A.ordinal()) {
                                        innerObjects.add(objRef);
                                        count++;
                                    }
                                }
                            }
                            //---------------------------------------------------------------
                        }
                        int finalCount = count;
                        objectCounts.computeIfPresent(object.getKlassIndex(), (key, value) -> value + finalCount);
                    }
                }

                List<String> names = new ArrayList<>();
                List<Integer> data = new ArrayList<>();
                for (String name : classNames) {
                    Integer klassIndex = instanceKlasses.get(name);
                    if (klassIndex != null) {
                        data.add(objectCounts.get(klassIndex));
                        names.add(name);

                    }
                }
                names.add("Other objects");
                data.add(objectSize - objectCounts.values()
                        .stream()
                        .reduce(0, Integer::sum));
                names.add("Empty space");
                data.add(objects.length - objectSize);

                try {
                    queue.put(new Message(names, data, objects.length));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();
    }

}
