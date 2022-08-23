package jvm.heap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Heap {

    boolean isCachedStringObjRef(int objRef);

    boolean isEnabledCacheString();

    @Nullable
    Integer getStringRefFromPool(@Nonnull String str);

    void putStringRefToPool(@Nonnull String str, int strRef, int charArrayRef);

    void disableCacheString();

    int getObjectRef(@Nonnull InstanceObject object);

    int changeObject(int objectRef, @Nonnull InstanceObject object);

    @Nonnull
    InstanceObject getInstanceObject(int objectRef);

    void clearInstanceObject(int objectIndex);

    @Nonnull
    ReferenceTable getReferenceTable();

    @Nonnull
    MethodRepo getMethodRepo();

    @Nonnull
    KlassLoader getKlassLoader();

    int setInstanceKlass(InstanceKlass klass);

    int getInstanceKlassSize();

    void decrementInstanceObjectSize();

    InstanceKlass getInstanceKlass(int instKlassIndex);

    int getInstanceObjectSize() ;

    int getInstanceObjectCapacity();
}
