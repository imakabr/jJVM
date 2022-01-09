package jvm.examples.shapes;

import javax.annotation.Nonnull;
import java.util.Random;

public class Circle extends Shape {

    private final int x;
    private final int y;
    private final int velocity;

    public Circle(int x, int y, @Nonnull String color, int velocity, int time) {
        super("circle", color, time);
        this.x = x;
        this.y = y;
        this.velocity = velocity;
    }

    @Override
    @Nonnull
    public String draw(@Nonnull Random random) {
        return x + " " + y + " " + controller.draw(name, color, velocity, random);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
