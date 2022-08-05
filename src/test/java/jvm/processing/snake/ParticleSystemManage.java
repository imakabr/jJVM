package jvm.processing.snake;

import java.util.ArrayList;
import java.util.List;

class ParticleSystemManage {

    private final List<ParticleSystem> particleSystems = new ArrayList<>();

    public void draw() {
        for (int i = particleSystems.size() - 1; i >= 0; i--) {
            ParticleSystem ps = particleSystems.get(i);
            if (ps.isDead()) {
                particleSystems.remove(i);
            } else {
                ps.run();
            }
        }
    }

    public void add(ParticleSystem ps) {
        particleSystems.add(ps);
    }
}
