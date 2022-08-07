package jvm.processing.snake;

import processing.core.PImage;
import processing.core.PVector;

import javax.annotation.Nonnull;

public class Particle {
    private final PVector position;
    private final PVector velocity;
    private final PVector acceleration;
    private float lifespan;
    private final int lifespanStep;
    private final Color color;
    private final PVector size;
    private PImage image;
    private final boolean isImage;
    @Nonnull
    private final Sketch pApplet;


    private Particle(@Nonnull Sketch pApplet, PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, Color color, PVector size, boolean isImage) {
        this.pApplet = pApplet;
        this.position = position.copy();
        this.velocity = new PVector(pApplet.random(velocityRangeX.x, velocityRangeX.y), pApplet.random(velocityRangeY.x, velocityRangeY.y));
        this.acceleration = new PVector(pApplet.random(accelerationRangeX.x, accelerationRangeX.y), pApplet.random(accelerationRangeY.x, accelerationRangeY.y));
        this.lifespan = lifespan;
        this.color = color;
        this.size = size;
        this.isImage = isImage;
        this.lifespanStep = lifespanStep;
    }

    public Particle(@Nonnull Sketch pApplet, PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, PImage image) {
        this(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, null, null, true);
        this.image = image;
    }

    public Particle(@Nonnull Sketch pApplet, PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, Color color, PVector size) {
        this(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, color, size, false);
    }

    public void run() {
        update();
        display();
    }

    // Method to update position
    private void update() {
        velocity.add(acceleration);
        position.add(velocity);
        lifespan -= lifespanStep;
    }

    // Method to display
    private void display() {
        if (!isImage) {
            pApplet.fill(color.r, color.g, color.b, lifespan);
            pApplet.rect(position.x, position.y, size.x, size.y);
            pApplet.fill(255, 255, 255);
        } else {
            pApplet.image(image, position.x, position.y);
        }
    }

    // Is the particle still useful?
    public boolean isDead() {
        return lifespan < 0.0;
    }
}
