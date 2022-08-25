package jvm.heap;

import jvm.parser.Method;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MethodRepo {
    @Nonnull
    private final Method[] methodTable;
    @Nonnull
    private final Map<String, Integer> indexByName;
    private int count;

    public MethodRepo() {
        this.methodTable = new Method[10000];
        this.indexByName = new HashMap<>();
    }

    public int setMethod(@Nonnull Method method) {
        methodTable[count] = method;
        indexByName.put(method.getClassName() + "." + method.getNameAndType(), count);
        return count++;
    }

    @Nonnull
    public Method getMethod(int index) {
        return methodTable[index];
    }

    @Nonnull
    public Integer getIndexByName(@Nonnull String methodName) {
        return indexByName.get(methodName);
    }

}


