package jvm.processing.heap_test;

import processing.core.PApplet;

import static jvm.processing.heap_test.Sketch.*;

public abstract class Shape {

    protected int x;
    protected int y;
    protected Rgb color1;
    protected final PApplet pApplet;
    private boolean alive;


    public Shape(PApplet pApplet, int x, int y, Rgb color1) {
        this.pApplet = pApplet;
        this.x = x;
        this.y = y;
        this.color1 = color1;
        this.alive = true;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public abstract void draw();

    public abstract void stopDraw();

    protected void polygon(float x, float y, float radius, int npoints) {
        float angle = TWO_PI / npoints;
        pApplet.beginShape();
        for (float a = 0; a < TWO_PI; a += angle) {
            float sx = x + cos(a) * radius;
            float sy = y + sin(a) * radius;
            pApplet.vertex(sx, sy);
        }
        pApplet.endShape(CLOSE);
    }

    protected void startDraw() {
        pApplet.pushMatrix();
        int center = height / 2 + (thickness / 2);
        pApplet.translate(startX + height * x + center, startY + height * y + center);
    }

    protected void endDraw() {
        pApplet.popMatrix();
    }

    protected void drawRedLines() {
        pApplet.stroke(255, 0, 0);
        pApplet.strokeWeight(thickness / 1.5f);
        pApplet.line(startX + height * x + thickness, startY + height * y + thickness, startX + height + height * x - thickness / 2, startY + height + height * y - thickness / 2);
        pApplet.line(startX + height * x + thickness, startY + height + height * y - thickness / 2, startX + height + height * x - thickness / 2, startY + height * y + thickness);
        pApplet.noStroke();
    }
}
