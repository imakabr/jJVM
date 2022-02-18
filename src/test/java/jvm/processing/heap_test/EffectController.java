package jvm.processing.heap_test;

import java.util.Random;
import processing.core.PApplet;

import static processing.core.PApplet.radians;

public class EffectController {

    private int degree;
    private float scale;
    private int sign;
    private final int velocity;
    private final int effect; // 0 - left, 1 - right, 2 - scale
    private final PApplet pApplet;

    public EffectController(PApplet pApplet, String effect, int velocity) {
        this.pApplet = pApplet;
        this.velocity = velocity;
        Random random = new Random();
        switch (effect) {
            case "left":
                sign = -1;
                degree = random.nextInt(360);
                this.effect = 0;
                break;
            case "right":
                sign = 1;
                degree = random.nextInt(360);
                this.effect = 1;
                break;
            case "scale":
                scale = random.nextFloat();
                sign = 1;
                this.effect = 2;
                break;
            default:
                throw new RuntimeException("wrong effect - |" + effect + "|");
        }
    }

    public void draw() {
        draw(false);
    }

    private void draw(boolean stop) {
        if (effect < 2) {
            if (stop) {
                stopDrawRotate();
            } else {
                drawRotate();
            }
        } else if (effect == 2) {
            if (stop) {
                stopDrawScale();
            } else {
                drawScale();
            }
        } else {
            throw new RuntimeException("wrong effect");
        }
    }

    public void stopDraw() {
        draw(true);
    }

    private void drawRotate() {
        pApplet.rotate(radians(degree));
        degree = (degree + velocity * sign) % 360;
    }

    private void stopDrawRotate() {
        pApplet.rotate(radians(degree));
    }

    private void drawScale() {
        int newSign = getSign(scale);
        sign = newSign == 0 ? sign : newSign;
        scale = scale + velocity * 0.008f * sign;
        pApplet.scale(scale);
    }

    private void stopDrawScale() {
        pApplet.scale(scale);
    }

    private int getSign(float number) {
        if (number >= 1) {
            return -1;
        } else if (number <= 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
