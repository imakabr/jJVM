package jvm.util;

public class RandomJVM {

    public RandomJVM() {
        initRandomJVM();
    }

    private native void initRandomJVM();
}
