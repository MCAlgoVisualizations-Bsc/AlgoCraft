package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public abstract class AbstractParticleDisplay implements IDisplayValue {
    protected final BlockDisplay base;

    private Instance instance;
    private Task task;

    protected AbstractParticleDisplay(BlockDisplay base) {
        this.base = base;
    }

    protected Particle particle() {
        return Particle.CRIT;
    }

    protected abstract List<Pos> targets();

    protected Pos particleSource() {
        return base.getPos();
    }

    protected List<Pos> particleSources() {
        Pos source = particleSource();
        return source == null ? List.of() : Collections.singletonList(source);
    }

    protected int particlesPerStep() {
        return 1;
    }

    protected int stepsForDistance(double distance) {
        return Math.max(5, (int) Math.ceil(distance * 6.0));
    }

    protected final List<Pos> nonNullTargets(Pos... positions) {
        List<Pos> out = new ArrayList<>(positions.length);
        for (Pos position : positions) {
            if (position != null) {
                out.add(position);
            }
        }
        return out;
    }

    @Override
    public Pos getPos() {
        return base.getPos();
    }

    @Override
    public void setInstance(Instance instance) {
        this.instance = instance;
        base.setInstance(instance);
        restartTaskIfNeeded();
    }

    @Override
    public void addViewer(Player player) {
        base.addViewer(player);
    }

    @Override
    public void remove() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        base.remove();
    }

    @Override
    public void teleport(Pos pos) {
        base.teleport(pos);
    }

    @Override
    public void setGlowing(boolean highlighted) {
        base.setGlowing(highlighted);
    }

    @Override
    public boolean isSpawned() {
        return base.isSpawned();
    }

    protected final void emitParticles() {
        if (instance == null || instance.getPlayers().isEmpty()) {
            return;
        }

        List<Pos> sources = particleSources();
        List<Pos> targets = targets();

        if (sources.size() == targets.size() && !sources.isEmpty()) {
            for (int i = 0; i < targets.size(); i++) {
                emitLine(sources.get(i), targets.get(i));
            }
            return;
        }

        Pos from = particleSource();
        for (Pos to : targets) {
            emitLine(from, to);
        }
    }

    private void restartTaskIfNeeded() {
        if (task != null && task.isAlive()) {
            return;
        }

        if (!hasAnyTarget()) {
            return;
        }

        task = MinecraftServer.getSchedulerManager()
                .buildTask(this::emitParticles)
                .executionType(ExecutionType.TICK_END)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private boolean hasAnyTarget() {
        for (Pos target : targets()) {
            if (target != null) {
                return true;
            }
        }
        return false;
    }

    protected final void emitLine(Pos from, Pos to) {
        if (to == null) {
            return;
        }

        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(2, stepsForDistance(distance));

        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            Pos point = new Pos(
                    from.x() + (dx * t),
                    from.y() + (dy * t),
                    from.z() + (dz * t)
            );
            ParticlePacket packet = new ParticlePacket(
                    particle(),
                    true,
                    true,
                    point,
                    new Vec(0, 0, 0),
                    0f,
                    Math.max(1, particlesPerStep())
            );
            for (var viewer : instance.getPlayers()) {
                viewer.sendPacket(packet);
            }
        }
    }
}



