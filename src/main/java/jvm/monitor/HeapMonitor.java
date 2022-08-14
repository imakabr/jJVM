package jvm.monitor;

import jvm.JVMType;
import jvm.heap.Heap;
import jvm.heap.InstanceKlass;
import jvm.heap.InstanceObject;
import jvm.heap.ReferenceTable;
import processing.core.PApplet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static jvm.Utils.getPureValue;

public class HeapMonitor {

    public static final String WITHOUT_GROUP = "withoutGroup";

    @Nonnull
    private final Heap heap;
    @Nonnull
    private final Set<String> classNames = new HashSet<>();
    @Nonnull
    private final Map<String, Set<String>> groups = new HashMap<>();
    @Nonnull
    public static final BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);


    public HeapMonitor(@Nonnull Heap heap, @Nonnull Set<String> classNames) {
        this.heap = heap;
        parseClassNames(classNames);
    }

    private void parseClassNames(@Nonnull Set<String> strings) {
        Set<String> withoutGroup = new HashSet<>();
        for (String str : strings) {
            if (str.contains("{")) {
                Set<String> classNameSet = new HashSet<>();
                String[] names = str.substring(str.indexOf("{") + 1, str.indexOf("}")).split(",");
                for (String name : names) {
                    classNameSet.add(name.trim());
                }
                String groupName = str.substring(0, str.indexOf("{")).trim();
                groups.put(groupName, classNameSet);
                classNames.addAll(classNameSet);
            } else {
                withoutGroup.add(str);
            }
        }
        groups.put(WITHOUT_GROUP, withoutGroup);
        classNames.addAll(withoutGroup);
    }

    public void run() {
        new Thread(() -> {
            new Thread(() -> PApplet.main("jvm.monitor.Sketch")).start();
            InstanceObject[] objects = heap.getInstanceObjects();
            InstanceKlass[] klasses = heap.getInstanceKlasses();
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
                int objectSize = heap.getInstanceObjectSize();
                Map<Integer, Integer> innerArrays = new HashMap<>(); // object reference -> instance klass index
                for (int i = 0; i < objectSize; i++) {
                    InstanceObject object = objects[i];
                    if (object != null && objectCounts.containsKey(object.getKlassIndex())) {
                        for (String fieldName : object.getFieldNames()) {
                            if (fieldName.contains("[")) {
                                String type = fieldName.substring(fieldName.lastIndexOf("[") + 1);
                                switch (type) {
                                    case "Z":
                                    case "C":
                                    case "F":
                                    case "D":
                                    case "B":
                                    case "S":
                                    case "I":
                                    case "J":
                                        int objRef = getPureValue(object.getValue(object.getIndexByFieldName(fieldName)));
                                        innerArrays.put(objRef, object.getKlassIndex());
                                }
                            }
                        }
                        objectCounts.computeIfPresent(object.getKlassIndex(), (key, value) -> value + 1);
                    }
                }

                countInnerArrays(innerArrays, objectCounts, heap.getReferenceTable(), objects);

                List<String> names = new ArrayList<>();
                List<Integer> data = new ArrayList<>();
                for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                    if (entry.getKey().equals(WITHOUT_GROUP)) {
                        for (String name : entry.getValue()) {
                            Integer klassIndex = instanceKlasses.get(name);
                            if (klassIndex != null) {
                                names.add(name);
                                data.add(objectCounts.get(klassIndex));
                            }
                        }
                    } else {
                        int count = 0;
                        for (String name : entry.getValue()) {
                            Integer klassIndex = instanceKlasses.get(name);
                            if (klassIndex != null) {
                                count += objectCounts.get(klassIndex);
                            }
                        }
                        names.add(entry.getKey());
                        data.add(count);
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

    private void countInnerArrays(@Nonnull Map<Integer, Integer> innerArrays,
                                  @Nonnull Map<Integer, Integer> objectCounts,
                                  @Nonnull ReferenceTable refTable,
                                  @Nonnull InstanceObject[] objects) {
        ArrayDeque<Integer> queue = new ArrayDeque<>(innerArrays.keySet());
        while (!queue.isEmpty()) {
            int objRef = queue.pop();
            int objIndex = refTable.getInstanceObjectIndex(objRef);
            InstanceObject innerObj = objIndex != -1 ? objects[objIndex] : null;
            if (innerObj != null && innerObj.isArray()) {
                objectCounts.computeIfPresent(innerArrays.get(objRef), (key, value) -> value + 1);
                if (innerObj.getValueType() == JVMType.A) {
                    for (long value : innerObj.getFieldValues()) {
                        queue.add(getPureValue(value));
                        innerArrays.put(getPureValue(value), innerArrays.get(objRef));
                    }
                }
            }
        }
    }

}
