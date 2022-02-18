package jvm.processing.heap_test.shapes;

import jvm.processing.heap_test.EffectController;
import jvm.processing.heap_test.Rgb;
import jvm.processing.heap_test.Shape;
import processing.core.PApplet;

import static jvm.processing.heap_test.Sketch.thickness;
import static jvm.processing.heap_test.Sketch.height;

public class Circle extends Shape {

    private final EffectController controller;

    public Circle(PApplet pApplet, int x, int y, Rgb color1, EffectController controller) {
        super(pApplet, x, y, color1);
        this.controller = controller;
    }

    public void draw() {
        startDraw();
        controller.draw();
        drawCircle(color1.a, color1.b, color1.c);
        endDraw();
    }

    public void stopDraw() {
        startDraw();
        controller.stopDraw();
        drawCircle(220, 220, 220);
        endDraw();
        drawRedLines();
    }

    private void drawCircle(int a, int b, int c) {
        pApplet.fill(a, b, c);
        polygon(0, 0, height / 3f, 20);
        pApplet.fill(0, 0, 0);
        pApplet.rect(-thickness * 2, -height / 4f + thickness, thickness, height/2f - thickness);
        pApplet.rect(-height / 4f + thickness, -thickness * 2, height/2f - thickness, thickness);
    }
}
