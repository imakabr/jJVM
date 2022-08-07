package jvm.processing.snake;

import processing.core.PImage;
import processing.core.PVector;

import javax.annotation.Nonnull;

class ParticleSystemBuilder {
    private PVector position;
    private PVector velocityRangeX;
    private PVector velocityRangeY;
    private PVector accelerationRangeX;
    private PVector accelerationRangeY;
    private float lifespan;
    private int lifespanStep;
    private int count;
    private Color[] color;
    private PVector[] size;
    private PImage image;
    private final Color[][] colors = new Color[][]{
            { //green
                    new Color(0, 202, 0),
                    new Color(0, 170, 0),
                    new Color(0, 230, 0),
                    new Color(0, 140, 0),
                    new Color(0, 100, 0)},
            { //blue
                    new Color(72, 100, 230),
                    new Color(0, 0, 255),
                    new Color(0, 0, 255),
                    new Color(72, 100, 230),
                    new Color(0, 0, 255)},
            { //red
                    new Color(136, 0, 21),
                    new Color(237, 28, 36),
                    new Color(239, 48, 57),
                    new Color(237, 28, 36),
                    new Color(255, 0, 0)},
            { //purple
                    new Color(157, 21, 166),
                    new Color(115, 14, 122),
                    new Color(238, 22, 252),
                    new Color(192, 24, 203),
                    new Color(157, 21, 166)}};
    @Nonnull
    private final Sketch pApplet;

    public ParticleSystemBuilder(Sketch pApplet) {
        this.pApplet = pApplet;
    }

    ParticleSystemBuilder position(PVector position) {
        this.position = position;
        return this;
    }

    ParticleSystemBuilder velocity(PVector velocityRangeX, PVector velocityRangeY) {
        this.velocityRangeX = velocityRangeX;
        this.velocityRangeY = velocityRangeY;
        return this;
    }

    ParticleSystemBuilder acceleration(PVector accelerationRangeX, PVector accelerationRangeY) {
        this.accelerationRangeX = accelerationRangeX;
        this.accelerationRangeY = accelerationRangeY;
        return this;
    }

    ParticleSystemBuilder lifespan(float lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    ParticleSystemBuilder count(int count) {
        this.count = count;
        return this;
    }

    ParticleSystemBuilder color2(Color[] color) {
        this.color = color;
        return this;
    }

    ParticleSystemBuilder sizeP(PVector[] size) {
        this.size = size;
        return this;
    }

    ParticleSystemBuilder image(PImage image) {
        this.image = image;
        return this;
    }

    ParticleSystemBuilder lifespanStep(int lifespanStep) {
        this.lifespanStep = lifespanStep;
        return this;
    }

    ParticleSystem build() {
        return new ParticleSystem(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, count, color, size);
    }

    ParticleSystem buildImage() {
        return new ParticleSystem(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, count, image);
    }

    ParticleSystem bricks(PVector position) {
        position(position);
        velocity(new PVector(-5, 5), new PVector(-5, 5));
        acceleration(new PVector(0, 0), new PVector(0.5f, 0.5f));
        lifespan(300);
        lifespanStep(8);
        count(50);
        color2(new Color[]{new Color(150, 0, 0), new Color(0, 0, 0), new Color(120, 0, 0)});
        sizeP(new PVector[]{new PVector(3, 3), new PVector(4, 4), new PVector(2, 2)});
        return build();
    }

    ParticleSystem bang(PVector position) {
        position(position);
        velocity(new PVector(-4, 4), new PVector(-4, 4));
        acceleration(new PVector(-1, 1), new PVector(-1, 1));
        lifespan(250);
        lifespanStep(17);
        count(40);
        color2(new Color[]{new Color(255, 242, 0), new Color(255, 240, 100), new Color(255, 250, 200)});
        sizeP(new PVector[]{new PVector(1, 1)});
        return build();
    }

    ParticleSystem bloods(PVector position, int number) {
        position(position);
        velocity(new PVector(-0.3f, 0.3f), new PVector(-0.5f, 5));
        acceleration(new PVector(-0.2f, 0.2f), new PVector(-0.1f, 1.5f));
        lifespan(250);
        lifespanStep(8);
        color2(colors[number]);
        sizeP(new PVector[]{new PVector(3, 3), new PVector(1, 1), new PVector(1, 1), new PVector(1, 1)});
        return build();
    }

    ParticleSystem images(PVector position, PImage image) {
        position(position);
        velocity(new PVector(-5, 5), new PVector(-5, 5));
        acceleration(new PVector(0, 0), new PVector(0.5f, 0.5f));
        lifespan(500);
        image(image);
        return buildImage();
    }
}
