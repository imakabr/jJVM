package jvm.processing.heap_test.shapes;

import jvm.processing.heap_test.EffectController;
import jvm.processing.heap_test.Rgb;
import jvm.processing.heap_test.Shape;
import processing.core.PApplet;
import static jvm.processing.heap_test.Sketch.height;

public class Pentagon extends Shape {

    private final EffectController controller;

    public Pentagon(PApplet pApplet, int x, int y, Rgb color1, EffectController controller) {
        super(pApplet, x, y, color1);
        this.controller = controller;
    }

    public void draw() {
        startDraw();
        controller.draw();
        pApplet.fill(color1.a, color1.b, color1.c);
        polygon(0, 0, height / 3f, 5);
        endDraw();
    }

    public void stopDraw() {
        startDraw();
        controller.stopDraw();
        pApplet.fill(220,220,220);
        polygon(0, 0, height / 3f, 5);
        endDraw();
        drawRedLines();
    }
}
