package io.github.mcalgovisualizations.prefab.algorithms.sort.insertion;

import io.github.mcalgovisualizations.prefab.Displays.EntityCreatureDisplay;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InsertionScene extends AbstractScene {

    private final Map<Integer, Pos> homePositions = new HashMap<>();
    private final Map<Integer, EntityCreatureDisplay> coversBySlot = new HashMap<>();
    private final Set<Integer> revealedSlots = new HashSet<>();

    private EntityCreatureDisplay iTracker;
    private Integer trackedISlot;

    private boolean initialPrefixRevealed = false;

    public InsertionScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        clearITracker();
        clearCovers();

        displaysBySlot.clear();
        homePositions.clear();
        revealedSlots.clear();
        initialPrefixRevealed = false;

        for (int i = 0; i < layoutResults.length; i++) {
            var result = layoutResults[i];
            if (result == null) continue;

            var display = (EntityCreatureDisplay) result.displayValue();
            var pos = result.pos();

            displaysBySlot.put(i, display);
            homePositions.put(i, pos);

            coverSlot(i);
        }
    }

    public void revealInitialPrefixIfNeeded() {
        if (initialPrefixRevealed) return;

        revealSlot(0);
        initialPrefixRevealed = true;
    }

    public void revealSlot(int slot) {
        if (revealedSlots.contains(slot)) return;

        var display = requireDisplay(slot);
        var home = requireHomePosition(slot);

        var cover = coversBySlot.remove(slot);
        if (cover != null) {
            cover.remove();
        }

        display.setInstance(instance, home);
        display.lookAt(origin);

        revealedSlots.add(slot);

        spawnParticleAuraBySlot(slot, Particle.END_ROD);
        playSound("block.amethyst_block.chime", 0.5f, 1.4f);
    }

    private void coverSlot(int slot) {
        var home = requireHomePosition(slot);
        var coverPos = home.add(0, 0.4, 0);

        var cover = new EntityCreatureDisplay(
                coverPos,
                EntityType.SHULKER,
                "X",
                true
        );

        cover.setInstance(instance);
        cover.lookAt(origin);

        coversBySlot.put(slot, cover);
    }


    public void markJ(int slot) {
        if (!revealedSlots.contains(slot)) return;
        spawnParticleAuraBySlot(slot, Particle.COMPOSTER);
    }

    public void markInserted(int slot) {
        if (!revealedSlots.contains(slot)) return;
        spawnParticleAuraBySlot(slot, Particle.END_ROD);
    }

    public void comparePulse(int leftSlot, int rightSlot) {
        if (!revealedSlots.contains(leftSlot) || !revealedSlots.contains(rightSlot)) {
            return;
        }

        var left = requireDisplay(leftSlot);
        var right = requireDisplay(rightSlot);

        left.lookAt(right.getPos().add(0, right.getEyeHeight(), 0));
        right.lookAt(left.getPos().add(0, left.getEyeHeight(), 0));

        spawnParticleAuraBySlot(leftSlot, Particle.HAPPY_VILLAGER);
        spawnParticleAuraBySlot(rightSlot, Particle.HAPPY_VILLAGER);

    }

    public void danceSwap(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) return;
        if (!revealedSlots.contains(leftSlot) || !revealedSlots.contains(rightSlot)) return;

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

    }

    public void commitSwap(int leftSlot, int rightSlot) {
        if (leftSlot == rightSlot) return;
        if (!revealedSlots.contains(leftSlot) || !revealedSlots.contains(rightSlot)) return;

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

    }

    public void finishInnerLoopVisuals() {
        resetRevealedDisplaysToHome();
    }

    public void resetRevealedDisplaysToHome() {
        for (int slot : revealedSlots) {
            var display = requireDisplay(slot);
            display.teleport(requireHomePosition(slot));
            lookAtOrigin(display);
        }

    }

    public void clearITracker() {
        trackedISlot = null;

        if (iTracker != null) {
            iTracker.remove();
            iTracker = null;
        }
    }

    private void clearCovers() {
        for (var cover : coversBySlot.values()) {
            cover.remove();
        }

        coversBySlot.clear();
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
        spawnParticleAuraAt(
                display.getPos().add(0, display.getEyeHeight(), 0),
                particle
        );
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
        clearCovers();
        super.cleanUp();

        homePositions.clear();
        revealedSlots.clear();
    }
}