package jvm.heap;

import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;
import jvm.heap.concurrent.InstanceObjectVolImpl;
import jvm.heap.sequential.InstanceObjectImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class InstanceObjectFactory {

    private static boolean heapMonitor;

    public static void setHeapMonitor(boolean heapMonitor) {
        InstanceObjectFactory.heapMonitor = heapMonitor;
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nullable InstanceObject objectFromStaticContent,
                                                   @Nullable String staticContentKlassName,
                                                   @Nonnull Heap heap,
                                                   @Nonnull List<String> fields,
                                                   int klassIndex) {
        return heapMonitor ? new InstanceObjectVolImpl(objectFromStaticContent, staticContentKlassName, heap, fields, klassIndex)
                : new InstanceObjectImpl(objectFromStaticContent, staticContentKlassName, heap, fields, klassIndex);
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nonnull Heap heap,
                                                   @Nonnull String arrayType,
                                                   @Nonnull String valueType,
                                                   int size,
                                                   int klassIndex) {
        return heapMonitor ? new InstanceObjectVolImpl(heap, arrayType, valueType, size, klassIndex)
                : new InstanceObjectImpl(heap, arrayType, valueType, size, klassIndex);
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        return heapMonitor ? new InstanceObjectVolImpl(heap, fields, klassIndex)
                : new InstanceObjectImpl(heap, fields, klassIndex);
    }
}
