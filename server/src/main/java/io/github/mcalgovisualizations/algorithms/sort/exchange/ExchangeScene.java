package io.github.mcalgovisualizations.algorithms.sort.exchange;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExchangeScene extends AbstractScene {
    private final Map<Integer, Pos> homePositions = new HashMap<>();

    /**
     * The logical slot currently standing at origin.
     */
    private Integer stagedSlot;

    public ExchangeScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int slot = 0; slot < layoutResults.length; slot++) {
            final var layout = layoutResults[slot];
            final var display = layout.displayValue();

            addDisplay(slot, display);
            display.setInstance(instance);

            homePositions.put(slot, layout.pos());

            requireEntityCreature(slot).lookAt(origin);
        }
    }

    /**
     * Move the current min for slot i to the stage.
     * The stage is origin.
     */
    public CompletableFuture<Void> stageCurrentMin(int slot) {
        return flushStage().thenCompose(_ -> {
            stagedSlot = slot;

            final var display = requireEntityCreature(slot);
            display.lookAt(origin);

            return display.walkTo(origin);
        });
    }

    /**
     * Compare staged current min with slot j.
     * Slot j stays at its home position.
     */
    public void compareSlot(int slot) {
        if (stagedSlot == null) {
            return;
        }

        final var stagedDisplay = requireEntityCreature(stagedSlot);
        final var comparedDisplay = requireEntityCreature(slot);

        stagedDisplay.lookAt(comparedDisplay);
        comparedDisplay.lookAt(stagedDisplay);
    }

    /**
     * Swap staged current min with slot j.
     *
     * Before:
     * - logical slot i is at origin
     * - logical slot j is at home j
     *
     * After:
     * - old i walks to home j
     * - old j walks to origin
     * - slot mapping is swapped
     * - origin still represents logical slot i
     */
    public CompletableFuture<Void> swapCurrentMinWithSlot(int i, int j) {
        if (stagedSlot == null) {
            throw new IllegalStateException("Cannot swap: no current min is staged");
        }

        if (!stagedSlot.equals(i)) {
            throw new IllegalStateException(
                    "Cannot swap: expected staged slot " + i + ", but found " + stagedSlot
            );
        }

        final var iDisplay = requireEntityCreature(i);
        final var jDisplay = requireEntityCreature(j);

        displaysBySlot.put(i, jDisplay);
        displaysBySlot.put(j, iDisplay);

        stagedSlot = i;

        jDisplay.lookAt(iDisplay);
        iDisplay.lookAt(jDisplay);

        final var oldMinWalk = iDisplay.walkTo(requireHomePosition(j));
        final var newMinWalk = jDisplay.walkTo(origin);

        return CompletableFuture.allOf(oldMinWalk, newMinWalk);
    }

    /**
     * Return staged entity to its logical home position.
     */
    public CompletableFuture<Void> flushStage() {
        if (stagedSlot == null) {
            return CompletableFuture.completedFuture(null);
        }

        final var slot = stagedSlot;
        stagedSlot = null;

        final var display = requireEntityCreature(slot);
        display.lookAt(origin);

        return display.walkTo(requireHomePosition(slot));
    }

    public CompletableFuture<Void> markSorted(int slot) {
        return flushStage().thenRun(() -> {
            spawnParticleAuraBySlot(slot, Particle.TOTEM_OF_UNDYING);
            requireEntityCreature(slot).lookAt(origin);
        });
    }

    public void highlightCurrentMin() {
        if (stagedSlot == null) {
            return;
        }

        spawnParticleAuraBySlot(stagedSlot, Particle.HAPPY_VILLAGER);
    }

    public void highlightSlot(int slot) {
        spawnParticleAuraBySlot(slot, Particle.ELECTRIC_SPARK);
    }

    public void highlightIndex(int slot) {
        spawnParticleAuraAt(
                requireHomePosition(slot).add(0, 1.2, 0),
                Particle.END_ROD
        );
    }

    public void resetLookAt() {
        displaysBySlot.values().forEach(display ->
                ((EntityCreatureDisplay) display).lookAt(origin)
        );
    }

    public void spawnParticleAuraBySlot(int slot, Particle particle) {
        final var display = requireEntityCreature(slot);

        spawnParticleAuraAt(
                display.getPos().add(0, display.getEyeHeight(), 0),
                particle
        );
    }

    private void spawnParticleAuraAt(Pos center, Particle particle) {
        if (instance == null) {
            return;
        }

        for (int i = 0; i < 10; i++) {
            final var angle = (Math.PI * 2.0 * i) / 10.0;

            final var point = center.add(
                    Math.cos(angle) * 0.7,
                    0.2,
                    Math.sin(angle) * 0.7
            );

            spawnParticleAt(point, particle, 1);
        }
    }

    private void spawnParticleAt(Pos pos, Particle particle, int count) {
        if (instance == null) {
            return;
        }

        final var packet = new ParticlePacket(
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

    private Pos requireHomePosition(int slot) {
        final var pos = homePositions.get(slot);

        if (pos == null) {
            throw new IllegalStateException("No home position registered for slot " + slot);
        }

        return pos;
    }

    private EntityCreatureDisplay requireEntityCreature(int slot) {
        return (EntityCreatureDisplay) super.requireDisplay(slot);
    }
}