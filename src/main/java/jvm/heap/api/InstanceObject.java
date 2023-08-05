package jvm.heap.api;


public interface InstanceObject {

    boolean isArray();

    int getFieldCount();

    void setFieldValue(int index, long value);

    long getFieldValue(int fieldIndex);

    int getKlassIndex();

}
