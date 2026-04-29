package io.github.mcalgovisualizations.algorithms.sort.selection;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SelectionScene extends AbstractScene {
    private final Set<Integer> sortedSlots = new HashSet<>();

    private Integer currentMinIndex = null;

    private EntityCreatureDisplay slimeTracker = null;

    public SelectionScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int i = 0; i < layoutResults.length; i++) {
            final var display = (EntityCreatureDisplay) layoutResults[i].displayValue();

            displaysBySlot.put(i, display);
            display.setInstance(instance, display.getPos().add(0, -1, 0));
            display.lookAt(origin);

            setHighlighted(i, false);
        }

        clearGlowing();
    }

    public void compare(int slot1, int slot2) {
        final var display1 = (EntityCreatureDisplay) requireDisplay(slot1);
        final var display2 = (EntityCreatureDisplay) requireDisplay(slot2);

        display1.lookAt(display2);
    }

    public void trackI(int slot) {
        var dv = ((EntityCreatureDisplay) requireDisplay(slot));
        var offset = dv.getEyeHeight() + 2;
        var pos = dv.getPos().add(0, offset, 0);

        if(slimeTracker == null) {
            slimeTracker = new EntityCreatureDisplay(pos, EntityType.SLIME, "Current I-index", true);
            slimeTracker.setInstance(instance);
        }

        slimeTracker.teleport(pos);
        slimeTracker.lookAt(origin);
    }

    public void resetLook() {
        displaysBySlot.values().forEach(dv -> ((EntityCreatureDisplay) dv).lookAt(origin));
    }

    public void trackJ(int slot) {
        clearGlowing();

        if (!isSorted(slot)) {
            setHighlighted(slot, true);
        }

        restoreCurrentMin();
        restoreSortedHighlights();
    }

    public void trackMinIndex(int slot) {
        clearGlowing();

        currentMinIndex = slot;

        if (!isSorted(slot)) {
            setHighlighted(slot, true);
        }

        restoreSortedHighlights();
    }

    public void swap(int slot1, int slot2) {
        clearGlowing();

        super.swapSlots(slot1, slot2);

        currentMinIndex = null;

        restoreSortedHighlights();
    }

    public void markSorted(int slot) {
        sortedSlots.add(slot);

        clearGlowing();
        currentMinIndex = null;

        setHighlighted(slot, true);
        restoreSortedHighlights();
    }

    public boolean isSorted(int slot) {
        return sortedSlots.contains(slot);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        sortedSlots.clear();
        currentMinIndex = null;
        if(slimeTracker != null)
            slimeTracker.kill();
        slimeTracker = null;
    }

    private void restoreCurrentMin() {
        if (currentMinIndex != null && !isSorted(currentMinIndex)) {
            setHighlighted(currentMinIndex, true);
        }
    }

    private void restoreSortedHighlights() {
        for (Integer slot : sortedSlots) {
            setHighlighted(slot, true);
        }
    }
}