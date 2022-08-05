package jvm.processing.snake;

import processing.core.PImage;
import processing.core.PVector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

class ParticleSystem {
    @Nonnull
    private final List<Particle> particles;
    @Nonnull
    private final Sketch pApplet;

    ParticleSystem(@Nonnull Sketch pApplet, PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, int count, Color[] color, PVector[] sizeParticle) {
        //origin = position.copy();
        this.particles = new ArrayList<>();
        this.pApplet = pApplet;
        for (int i = 0; i < count; i++) {
            addParticle(position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, color[i % color.length], sizeParticle[i % sizeParticle.length]);
        }
    }

    ParticleSystem(@Nonnull Sketch pApplet, PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, int count, PImage image) {
        //origin = position.copy();
        this.particles = new ArrayList<>();
        this.pApplet = pApplet;
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, image));
        }
    }

    private void addParticle(PVector position, PVector velocityRangeX, PVector velocityRangeY, PVector accelerationRangeX, PVector accelerationRangeY, float lifespan, int lifespanStep, Color color, PVector size) {
        particles.add(new Particle(pApplet, position, velocityRangeX, velocityRangeY, accelerationRangeX, accelerationRangeY, lifespan, lifespanStep, color, size));
    }

    public void run() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.run();
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }

    public boolean isDead() {
        return particles.size() == 0;
    }
}

