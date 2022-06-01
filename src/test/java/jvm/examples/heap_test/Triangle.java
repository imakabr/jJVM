package jvm.examples.heap_test;

import static jvm.examples.heap_test.Main.inHeap;

public class Triangle extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Triangle(int x, int y, String color, int velocity, int time, Service service) {
        super("triangle", color, time, service);
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
        clear(y + " " + x);
        inHeap[y][x] = false;
    }
}