package io.github.mcalgovisualizations.algorithms.sort.exchange;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExchangeScene extends AbstractScene {
    private final Map<Integer, Pos> homePositions = new HashMap<>();

    private final Pos centerPos;

    private Integer stagedSlot;

    public ExchangeScene(@NotNull SceneContext context) {
        super(context);
        this.centerPos = context.origin().add(0, 2.5, 0);
        this.instance.setBlock(context.origin(), Block.AMETHYST_BLOCK);
        this.instance.setBlock(context.origin().add(-1, 0, 0), Block.AMETHYST_BLOCK);
        this.instance.setBlock(context.origin().add(0, 0, -1), Block.AMETHYST_BLOCK);
        this.instance.setBlock(context.origin().add(-1, 0, -1), Block.AMETHYST_BLOCK);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int slot = 0; slot < layoutResults.length; slot++) {
            final var layout = layoutResults[slot];
            final var display = layout.displayValue();

            addDisplay(slot, display);
            display.setInstance(instance);

            homePositions.put(slot, layout.pos());

            requireEntityCreature(slot).lookAt(origin.add(0, 1.2, 0));
        }
    }

    /**
     * Current-min slot enters the center stage.
     */
    public void stageCurrentMin(int slot) {
        flushStage();

        stagedSlot = slot;

        final var display = requireEntityCreature(slot);
        moveDisplay(display, centerPos);
        display.lookAt(origin);
    }

    /**
     * Compare the staged current-min against challenger j.
     * The challenger stays in its array/home position.
     */
    public void compareChallenger(int challengerSlot) {
        if (stagedSlot == null) {
            return;
        }

        final var stagedDisplay = requireEntityCreature(stagedSlot);
        final var challengerDisplay = requireEntityCreature(challengerSlot);

        stagedDisplay.lookAt(challengerDisplay);
        challengerDisplay.lookAt(stagedDisplay);
    }

    /**
     * Called when challenger j is smaller than the staged current min.
     *
     * Before:
     * - slot i display is on center stage
     * - slot j display is still at home slot j
     *
     * After:
     * - old staged i moves to slot j
     * - challenger j moves to center stage
     * - internal mapping swaps
     * - center stage still represents logical slot i
     */
    public void swapCurrentMinWithChallenger(int i, int j) {
        if (stagedSlot == null) {
            throw new IllegalStateException("Cannot swap: no current min is staged");
        }

        if (!stagedSlot.equals(i)) {
            throw new IllegalStateException(
                    "Cannot swap: expected staged current min slot " + i + ", but found " + stagedSlot
            );
        }

        final var iDisplay = requireEntityCreature(i);
        final var jDisplay = requireEntityCreature(j);

        moveDisplay(iDisplay, requireHomePosition(j));
        moveDisplay(jDisplay, centerPos);

        displaysBySlot.put(i, jDisplay);
        displaysBySlot.put(j, iDisplay);

        jDisplay.lookAt(iDisplay);
        iDisplay.lookAt(jDisplay);

        /*
         * Important:
         * The display currently on stage is now the logical value of slot i.
         */
        stagedSlot = i;
    }

    /**
     * Return the staged current-min to its logical home slot.
     */
    public void flushStage() {
        if (stagedSlot == null) {
            return;
        }

        final var display = requireEntityCreature(stagedSlot);

        moveDisplay(display, requireHomePosition(stagedSlot));
        display.lookAt(origin);

        stagedSlot = null;
    }

    public void markSorted(int slot) {
        flushStage();
        spawnParticleAuraBySlot(slot, Particle.TOTEM_OF_UNDYING);
        requireEntityCreature(slot).lookAt(origin);
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

    public void resetLookAt() {
        displaysBySlot.values().forEach(display ->
                ((EntityCreatureDisplay) display).lookAt(origin)
        );
    }

    public Pos centerPos() {
        return centerPos;
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

    public void highlightIndex(int slot) {
        spawnParticleAuraAt(
                requireHomePosition(slot).add(0, 1.2, 0),
                Particle.END_ROD
        );
    }

    private void moveDisplay(EntityCreatureDisplay display, Pos pos) {
        display.teleport(pos);
    }
}