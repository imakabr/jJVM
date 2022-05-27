package jvm.examples.heap_test;

import static jvm.examples.heap_test.Main.inHeap;

public class Diamond extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Diamond(int x, int y, String color, int velocity, int time, Service service) {
        super("diamond", color, time, service);
        this.x = x;
        this.y = y;
        this.velocity = velocity;
    }

    @Override
    public String toString() {
        return y + " " + x + " " + super.toString() + " " + velocity + " " + service.getMoves();
    }

    @Override
    protected void finalize() throws Throwable {
        print(y + " " + x + " cleared");
        inHeap[y][x] = false;
    }
}