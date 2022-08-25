package jvm.heap;

import jvm.parser.Method;

import java.util.HashMap;
import java.util.Map;

public class MethodRepo {
    private final Method[] methodTable;
    private final Map<String, Integer> indexByName;
    private int count;

    public MethodRepo() {
        this.methodTable = new Method[10000];
        this.indexByName = new HashMap<>();
    }

    public int setMethod(Method method) {
        methodTable[count] = method;
        indexByName.put(method.getClassName() + "." + method.getNameAndType(), count);
        return count++;
    }

    public Method getMethod(int index) {
        return methodTable[index];
    }

    public Integer getIndexByName(String methodName) {
        return indexByName.get(methodName);
    }

}


