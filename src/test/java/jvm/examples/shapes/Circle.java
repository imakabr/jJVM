package jvm.examples.shapes;

import static jvm.examples.shapes.Main.inHeap;

public class Circle extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Circle(int x, int y, String color, int velocity, int time, Service service) {
        super("circle", color, time, service);
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
