package jvm.examples.garbage_collector;

public class MarkAndSweepExample {

    public Object createObjects() {
        GarbageObj[] objects = new GarbageObj[4];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new GarbageObj();
        }

        objects[2] = null;
        objects[3] = null;

        GarbageObj obj0 = new GarbageObj((objects[1]));
        objects[0].setObj(obj0);
        objects[1].setObj(objects[0]);

        GarbageObj obj1 = new GarbageObj();
        GarbageObj obj2 = new GarbageObj(obj1);
        obj1.setObj(obj2);

        return objects;
    }

}
