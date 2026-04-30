package io.github.mcalgovisualizations.algorithms.sort.insertion;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InsertionScene extends AbstractScene {

    private final Map<Integer, Pos> homePositions = new HashMap<>();

    private EntityCreatureDisplay iTracker;
    private Integer trackedISlot;

    public InsertionScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        clearITracker();

        displaysBySlot.clear();
        homePositions.clear();

        for (int i = 0; i < layoutResults.length; i++) {
            var result = layoutResults[i];
            if (result == null) continue;

            var display = (EntityCreatureDisplay) result.displayValue();
            var pos = result.pos();

            display.setInstance(instance, pos);
            display.lookAt(origin);

            displaysBySlot.put(i, display);
            homePositions.put(i, pos);
        }
    }

    public void trackI(int slot) {
        trackedISlot = slot;
        markVisitedBoundary(slot);
        refreshITracker();
    }

    public void markJ(int slot) {
        spawnParticleAuraBySlot(slot, Particle.COMPOSTER);
    }

    public void markPlaced(int slot) {
        spawnParticleAuraBySlot(slot, Particle.END_ROD);
    }

    public void comparePulse(int leftSlot, int rightSlot) {
        var left = requireDisplay(leftSlot);
        var right = requireDisplay(rightSlot);

        left.lookAt(right.getPos().add(0, right.getEyeHeight(), 0));
        right.lookAt(left.getPos().add(0, left.getEyeHeight(), 0));

        spawnParticleAuraBySlot(leftSlot, Particle.HAPPY_VILLAGER);
        spawnParticleAuraBySlot(rightSlot, Particle.HAPPY_VILLAGER);

        refreshITracker();
    }

    public void danceSwap(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) return;

        var left = requireDisplay(leftSlot);
        var right = requireDisplay(rightSlot);

        var leftHome = requireHomePosition(leftSlot);
        var rightHome = requireHomePosition(rightSlot);

        var middle = midpoint(leftHome, rightHome);

        var leftDance = middle.add(0, 1.2, -0.8);
        var rightDance = middle.add(0, 1.2, 0.8);

        left.teleport(leftDance);
        right.teleport(rightDance);

        left.lookAt(right.getPos().add(0, right.getEyeHeight(), 0));
        right.lookAt(left.getPos().add(0, left.getEyeHeight(), 0));

        spawnParticleAuraAt(leftDance, Particle.CRIT);
        spawnParticleAuraAt(rightDance, Particle.CRIT);

        refreshITracker();
    }

    public void commitSwap(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) return;

        var left = requireDisplay(leftSlot);
        var right = requireDisplay(rightSlot);

        var leftHome = requireHomePosition(leftSlot);
        var rightHome = requireHomePosition(rightSlot);

        left.teleport(rightHome);
        right.teleport(leftHome);

        lookAtOrigin(left);
        lookAtOrigin(right);

        displaysBySlot.put(leftSlot, right);
        displaysBySlot.put(rightSlot, left);

        refreshITracker();
    }

    public void finishInnerLoopVisuals() {
        resetAllDisplaysToHome();
    }

    public void resetAllDisplaysToHome() {
        for (var entry : homePositions.entrySet()) {
            int slot = entry.getKey();
            var display = requireDisplay(slot);

            display.teleport(entry.getValue());
            lookAtOrigin(display);
        }

        refreshITracker();
    }

    public void markVisitedBoundary(int slot) {
        if (slot < 0 || slot >= homePositions.size()) return;

        var currentHome = requireHomePosition(slot);
        var currentDisplay = requireDisplay(slot);

        spawnParticleAuraAt(
                currentHome.add(0, currentDisplay.getEyeHeight() + 0.9, 0),
                Particle.END_ROD
        );

        if (slot + 1 < homePositions.size()) {
            var nextHome = requireHomePosition(slot + 1);

            spawnParticleAuraAt(
                    nextHome.add(0, 0.4, 0),
                    Particle.SMOKE
            );
        }
    }

    public void clearITracker() {
        trackedISlot = null;

        if (iTracker != null) {
            iTracker.remove();
            iTracker = null;
        }
    }

    private void refreshITracker() {
        if (trackedISlot == null) return;

        var base = requireDisplay(trackedISlot).getPos();
        var tracked = requireDisplay(trackedISlot);
        var trackerPos = base.add(0, tracked.getEyeHeight() + 2, 0);

        if (iTracker == null) {
            iTracker = new EntityCreatureDisplay(trackerPos, EntityType.SLIME, "I", true);
            iTracker.setInstance(instance);
        }

        iTracker.teleport(trackerPos);
        iTracker.lookAt(base.add(0, tracked.getEyeHeight(), 0));
    }

    public EntityCreatureDisplay requireDisplay(int slot) {
        return (EntityCreatureDisplay) super.requireDisplay(slot);
    }

    private Pos requireHomePosition(int slot) {
        var pos = homePositions.get(slot);

        if (pos == null) {
            throw new IllegalArgumentException("No home position for slot " + slot);
        }

        return pos;
    }

    private Pos midpoint(Pos a, Pos b) {
        return new Pos(
                (a.x() + b.x()) / 2.0,
                (a.y() + b.y()) / 2.0,
                (a.z() + b.z()) / 2.0,
                0f,
                0f
        );
    }

    private void lookAtOrigin(IDisplayValue display) {
        if (display instanceof EntityCreatureDisplay creature) {
            creature.lookAt(origin);
        }
    }

    public void spawnParticleAuraBySlot(int slot, Particle particle) {
        var display = requireDisplay(slot);
        spawnParticleAuraAt(display.getPos().add(0, display.getEyeHeight(), 0), particle);
    }

    private void spawnParticleAuraAt(Pos center, Particle particle) {
        if (instance == null) return;

        for (int i = 0; i < 10; i++) {
            double angle = (Math.PI * 2.0 * i) / 10.0;

            var point = center.add(
                    Math.cos(angle) * 0.7,
                    0.2,
                    Math.sin(angle) * 0.7
            );

            spawnParticleAt(point, particle, 1);
        }
    }

    private void spawnParticleAt(Pos pos, Particle particle, int count) {
        if (instance == null) return;

        var packet = new ParticlePacket(
                particle,
                true,
                true,
                pos,
                Vec.ZERO,
                0f,
                count
        );

        for (var player : instance.getPlayers()) {
            player.sendPacket(packet);
        }
    }

    @Override
    public void cleanUp() {
        clearITracker();
        super.cleanUp();
        homePositions.clear();
    }
}