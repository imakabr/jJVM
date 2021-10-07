package jvm.heap;

import jvm.JVMType;
import jvm.JVMValue;
import jvm.parser.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceObject {
    private final JVMValue[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    private int klassIndex;

    public InstanceObject(List<Field> fields, int klassIndex) {
        this.fieldValues = new JVMValue[fields.size()];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        for (int fieldIndex = 0; fieldIndex<fieldValues.length; fieldIndex++) {
            fieldValues[fieldIndex] = new JVMValue(JVMType.I, 0);
            indexByFieldName.put(fields.get(fieldIndex).getName(), fieldIndex);
        }
    }

    public void setKlassIndex(int klassIndex) {
        this.klassIndex = klassIndex;
    }

    public void setValue(int index, JVMValue value) {
        fieldValues[index] = value;
    }

    public JVMValue getValue(int fieldIndex) {
        return fieldValues[fieldIndex];
    }

    public void setIndexByFieldName(String name, int index) {
        indexByFieldName.put(name, index);
    }

    public int getIndexByFieldName(String name) {
        return indexByFieldName.get(name);
    }

    public int getKlassIndex() {
        return klassIndex;
    }
}
