package jvm.examples.garbage_collector;

public class GarbageObj {

    private GarbageObj obj;

    public GarbageObj() {
    }

    public GarbageObj(GarbageObj obj) {
        this.obj = obj;
    }

    public void setObj(GarbageObj obj) {
        this.obj = obj;
    }
}
