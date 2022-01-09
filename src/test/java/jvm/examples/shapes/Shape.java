package jvm.examples.shapes;

import java.util.Random;

public class Shape {

    protected final String name;
    protected final String color;
    private int time;
    protected final Controller controller;

    public Shape(String name, String color, int time) {
        this.name = name;
        this.color = color;
        this.controller = new Controller();
        this.time = time;
    }

    public String draw(Random random) {
        return "Just shape";
    }

    public boolean isDead() {
        time--;
        return time <= 0;
    }
}
