package jvm.processing.heap_test;

import processing.core.PApplet;

public class Sketch extends PApplet {

    public static int startX = 50;
    public static int startY = 50;
    public static int height = 40;
    public static int count = 18;
    public static int thickness = 1;

    private final int port = 10003;

    Shape[][] shapes = new Shape[count][count];
    ShapeManager manager = new ShapeManager(this, new SocketReader(this, port));

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        noStroke();
        background(0);
        frameRate(80);
    }

    public void draw() {
        background(25, 25, 25);
        grid(startX, startY, height, count, thickness);
        manager.checkShape(shapes);

        for (int y = 0; y < count; y++) {
            for (int x = 0; x < count; x++) {
                Shape shape = shapes[y][x];
                if (shape != null) {
                    if (shape.isAlive()) {
                        shape.draw();
                    } else {
                        shape.stopDraw();
                    }
                }
            }
        }
    }

    void grid(int bX, int bY, int height, int count, int thickness) {
        fill(255, 255, 255);
        for (int i = 0; i < count + 1; i++) {
            rect(bX, bY + i * height, height * count, thickness);
            rect(bX + i * height, bY, thickness, height * count);
        }
    }
}
