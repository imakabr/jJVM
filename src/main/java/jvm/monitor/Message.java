package jvm.monitor;

import javax.annotation.Nonnull;
import java.util.List;

public class Message {

    @Nonnull
    private final List<String> classNames;
    @Nonnull
    private final List<Integer> data;
    private final int heapSize;

    public Message(@Nonnull List<String> classNames, @Nonnull List<Integer> data, int heapSize) {
        this.classNames = classNames;
        this.data = data;
        this.heapSize = heapSize;
    }

    @Nonnull
    public List<String> getClassNames() {
        return classNames;
    }

    @Nonnull
    public List<Integer> getData() {
        return data;
    }

    public int getHeapSize() {
        return heapSize;
    }
}
