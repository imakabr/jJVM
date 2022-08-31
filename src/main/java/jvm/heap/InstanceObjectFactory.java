package jvm.heap;

import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;
import jvm.heap.sequential.InstanceObjectImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class InstanceObjectFactory {

    private static boolean heapMonitor;

    public static void setHeapMonitor(boolean heapMonitor) {
        InstanceObjectFactory.heapMonitor = true;
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nullable InstanceObject objectFromStaticContent,
                                                   @Nullable String staticContentKlassName,
                                                   @Nonnull Heap heap,
                                                   @Nonnull List<String> fields,
                                                   int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(objectFromStaticContent, staticContentKlassName, heap, fields, klassIndex) : null;
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nonnull Heap heap,
                                                   @Nonnull String arrayType,
                                                   @Nonnull String valueType,
                                                   int size,
                                                   int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(heap, arrayType, valueType, size, klassIndex) : null;
    }

    @Nonnull
    public static InstanceObject getInstanceObject(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(heap, fields, klassIndex) : null;
    }
}
