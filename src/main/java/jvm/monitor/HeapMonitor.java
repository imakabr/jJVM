package jvm.monitor;

import jvm.heap.api.Heap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.heap.api.ReferenceTable;
import processing.core.PApplet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
            for (; ; ) {
                Map<String, Integer> instanceKlasses = new HashMap<>(); // class name -> instance klass index
                Map<Integer, Integer> objectCounts = new HashMap<>(); // instance klass index -> count

                initKlasses(instanceKlasses, objectCounts);
                countInstanceObjects(objectCounts);
                putMessage(objectCounts, instanceKlasses);
            }

        }).start();
    }

    private void initKlasses(@Nonnull Map<String, Integer> instanceKlasses,
                             @Nonnull Map<Integer, Integer> objectCounts) {
        int klassesSize = heap.getInstanceKlassSize();
        for (int klassIndex = 0; klassIndex < klassesSize; klassIndex++) {
            InstanceKlass klass = heap.getInstanceKlass(klassIndex);
            if (classNames.contains(klass.getName())) {
                instanceKlasses.putIfAbsent(klass.getName(), klassIndex);
                objectCounts.putIfAbsent(klassIndex, 0);
            }
        }
    }

    private void putMessage(@Nonnull Map<Integer, Integer> objectCounts,
                            @Nonnull Map<String, Integer> instanceKlasses) {
        List<String> names = new ArrayList<>();
        List<Integer> data = new ArrayList<>();

        collectData(names, data, objectCounts, instanceKlasses);

        try {
            queue.put(new Message(names, data, heap.getInstanceObjectCapacity()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void countInstanceObjects(@Nonnull Map<Integer, Integer> objectCounts) {
        ReferenceTable refTable = heap.getReferenceTable();
        for (int i = 0; i < refTable.size(); i++) {
            int objectIndex = refTable.getInstanceObjectIndex(i);
            InstanceObject object = objectIndex != -1 ? heap.getInstanceObjectByObjInd(objectIndex) : null;
            if (object != null && objectCounts.containsKey(object.getKlassIndex())) {
                objectCounts.computeIfPresent(object.getKlassIndex(), (key, value) -> value + 1);
            }
        }
    }

    private void collectData(@Nonnull List<String> names,
                             @Nonnull List<Integer> data,
                             @Nonnull Map<Integer, Integer> objectCounts,
                             @Nonnull Map<String, Integer> instanceKlasses) {
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
        int objectSize = heap.getInstanceObjectSize();
        data.add(objectSize - objectCounts.values()
                .stream()
                .reduce(0, Integer::sum));
        names.add("Empty space");
        data.add(heap.getInstanceObjectCapacity() - objectSize);
    }

}
