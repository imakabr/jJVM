package jvm.examples.shapes;

import java.util.Random;

public class Cross extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Cross(int x, int y, String color, int velocity, int time) {
        super("cross", color, time);
        this.x = x;
        this.y = y;
        this.velocity = velocity;
    }

    @Override
    public String draw(Random random) {
        return x + " " + y + " " + controller.draw(name, color, velocity, random);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
