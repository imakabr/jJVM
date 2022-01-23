package jvm.examples.shapes;

import static jvm.examples.shapes.Main.inHeap;

public class Star extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Star(int x, int y, String color, int velocity, int time, Service service) {
        super("star", color, time, service);
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
        print(y + " " + x + " empty");
        inHeap[y][x] = false;
    }
}
