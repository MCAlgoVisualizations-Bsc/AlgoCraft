package io.github.mcalgovisualizations.prefab.algorithms.sort.selection;

import io.github.mcalgovisualizations.prefab.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SelectionScene extends AbstractScene {
    private final Set<Integer> sortedSlots = new HashSet<>();

    private Integer currentMinIndex = null;
    private EntityCreatureDisplay slimeTracker = null;
    private Task frozenParticleTask = null;

    public SelectionScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int i = 0; i < layoutResults.length; i++) {
            final var display = (EntityCreatureDisplay) layoutResults[i].displayValue();

            displaysBySlot.put(i, display);
            display.setInstance(instance, display.getPos());
            display.lookAt(origin);

            setHighlighted(i, false);
        }

        clearGlowing();
    }

    public void spawnParticleAuraBySlot(int slot, Particle particle) {
        ((EntityCreatureDisplay) requireDisplay(slot)).spawnParticleAura(particle);
    }

    public void compare(int slot1, int slot2) {
        if (slimeTracker == null) return;

        final var display1 = (EntityCreatureDisplay) requireDisplay(slot1);

        slimeTracker.lookAt(display1);
        display1.lookAt(slimeTracker);
    }

    public void trackI(int slot) {
        var display = (EntityCreatureDisplay) requireDisplay(slot);
        var offset = display.getEyeHeight() + 2;
        var pos = display.getPos().add(0, offset, 0);

        if (slimeTracker == null) {
            slimeTracker = new EntityCreatureDisplay(pos, EntityType.SLIME, "Current I-index", true);
            slimeTracker.setInstance(instance);
        }

        slimeTracker.teleport(pos);
        slimeTracker.lookAt(origin);
    }

    public void resetLook() {
        displaysBySlot.values().forEach(display -> ((EntityCreatureDisplay) display).lookAt(origin));
    }

    public void trackJ(int slot) {
        clearGlowing();

        if (!isSorted(slot)) {
            setHighlighted(slot, true);
        }

        restoreCurrentMin();
        //restoreSortedHighlights();
    }

    public void trackMinIndex(int slot) {
        clearGlowing();

        currentMinIndex = slot;

        if (!isSorted(slot)) {
            setHighlighted(slot, true);
        }
    }

    public void swap(int slot1, int slot2) {
        clearGlowing();

        super.swapSlots(slot1, slot2);

        currentMinIndex = null;
    }

    public void markSorted(int slot) {
        sortedSlots.add(slot);

        clearGlowing();
        currentMinIndex = null;

        spawnParticleAuraBySlot(slot, Particle.SNOWFLAKE);
        startFrozenParticles();
    }

    public boolean isSorted(int slot) {
        return sortedSlots.contains(slot);
    }

    private void startFrozenParticles() {
        if (frozenParticleTask != null) return;

        frozenParticleTask = MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    for (Integer slot : sortedSlots) {
                        spawnParticleAuraBySlot(slot, Particle.SNOWFLAKE);
                    }
                })
                .repeat(10, TimeUnit.SERVER_TICK)
                .schedule();
    }

    @Override
    public void cleanUp() {
        super.cleanUp();

        if (frozenParticleTask != null) {
            frozenParticleTask.cancel();
            frozenParticleTask = null;
        }

        sortedSlots.clear();
        currentMinIndex = null;

        if (slimeTracker != null) {
            slimeTracker.kill();
            slimeTracker = null;
        }
    }

    private void restoreCurrentMin() {
        if (currentMinIndex != null && !isSorted(currentMinIndex)) {
            setHighlighted(currentMinIndex, true);
        }
    }
}