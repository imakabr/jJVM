package jvm.processing.heap_test.shapes;

import jvm.processing.heap_test.EffectController;
import jvm.processing.heap_test.Rgb;
import jvm.processing.heap_test.Shape;
import processing.core.PApplet;

import static jvm.processing.heap_test.Sketch.thickness;
import static jvm.processing.heap_test.Sketch.height;

public class Cross extends Shape {

    private final EffectController controller;

    public Cross(PApplet pApplet, int x, int y, Rgb color1, EffectController controller) {
        super(pApplet, x, y, color1);
        this.controller = controller;
    }

    public void draw() {
        startDraw();
        controller.draw();
        pApplet.fill(color1.a, color1.b, color1.c);
        pApplet.scale(1.3f);
        pApplet.rect(-thickness * 2, -height / 4f + thickness, thickness * 4, height / 2f - thickness * 2);
        pApplet.rect(-height / 4f + thickness, -thickness * 2, height / 2f - thickness * 2, thickness * 4);
        endDraw();
    }

    public void stopDraw() {
        startDraw();
        controller.stopDraw();
        pApplet.fill(220,220,220);
        pApplet.scale(1.3f);
        pApplet.rect(0 - thickness * 2, -height / 2 /2 + thickness, thickness * 4, height/2 - thickness * 2);
        pApplet.rect(-height / 2 / 2 + thickness, 0 - thickness * 2, height/2 - thickness * 2, thickness * 4);
        endDraw();
        drawRedLines();
    }
}
