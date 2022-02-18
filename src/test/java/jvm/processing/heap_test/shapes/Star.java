package jvm.processing.heap_test.shapes;

import jvm.processing.heap_test.EffectController;
import jvm.processing.heap_test.Rgb;
import jvm.processing.heap_test.Shape;
import processing.core.PApplet;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.TWO_PI;
import static jvm.processing.heap_test.Sketch.height;

public class Star extends Shape {

    private final EffectController controller;

    public Star(PApplet pApplet, int x, int y, Rgb color1, EffectController controller) {
        super(pApplet, x, y, color1);
        this.controller = controller;
    }

    public void draw() {
        startDraw();
        controller.draw();
        pApplet.fill(color1.a, color1.b, color1.c);
        star(0, 0, height / 3f, height / 6f, 5);
        endDraw();
    }

    public void stopDraw() {
        startDraw();
        controller.stopDraw();
        pApplet.fill(220, 220, 220);
        star(0, 0, height / 3f, height / 6f,5);
        endDraw();
        drawRedLines();
    }

    private void star(float x, float y, float radius1, float radius2, int npoints) {
        float angle = TWO_PI / npoints;
        float halfAngle = angle/2.0f;
        pApplet.beginShape();
        for (float a = 0; a < TWO_PI; a += angle) {
            float sx = x + cos(a) * radius2;
            float sy = y + sin(a) * radius2;
            pApplet.vertex(sx, sy);
            sx = x + cos(a+halfAngle) * radius1;
            sy = y + sin(a+halfAngle) * radius1;
            pApplet.vertex(sx, sy);
        }
        pApplet.endShape(CLOSE);
    }
}
