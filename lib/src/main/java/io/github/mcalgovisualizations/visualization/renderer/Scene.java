package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import io.github.mcalgovisualizations.visualization.renderer.Displays.BlockDisplay;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Scene = Minestom world state.
 *
 */
public final class Scene implements ISceneOps {

    private final Instance instance;
    private final Pos origin;
    private final List<Player> viewers = new ArrayList<>();
    private final AudienceChannel audience;


    // Stable identity mapping (slot -> display wrapper/entity)
    private final Map<Integer, IDisplayValue> displaysBySlot =
            new HashMap<>();

    // Floating hologram above the visualization
    private HologramDisplay hologram;

    // Visual state
    private final Set<Integer> highlightedSlots = new HashSet<>();
    private final Map<Integer, CellState> slotStates = new HashMap<>();

    private boolean started = false;

    public Scene(@NotNull Instance instance, @NotNull Pos origin, @NotNull AudienceChannel audience) {
        this.instance = instance;
        this.origin = origin;
        this.audience = audience;
    }

    @Override
    public <T extends Comparable<T>> void setLayout(LayoutResult<T>[] layoutResults) {
        Objects.requireNonNull(layoutResults);
        cleanUp();
        this.started = true;

        boolean useBlockGridDisplay = isAStarGrid(layoutResults);

        for(int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            IDisplayValue dv;
            if (useBlockGridDisplay) {
                CellState initialState = initialCellState(value.value());
                Block initialBlock = blockForState(initialState);
                dv = new BlockDisplay(instance, pos, initialBlock, value.toString(), false);
                slotStates.put(i, initialState);
            } else {
                dv = layoutResults[i].getDisplayValue();
            }

            displaysBySlot.put(i, dv);
            dv.setInstance(instance);
        }

        // Create hologram dynamicallyy floating above the center of the layout
        if (layoutResults.length > 0) {
            double sumX = 0.0;
            double sumY = 0.0;
            double sumZ = 0.0;
            for (var lr : layoutResults) {
                var p = lr.pos();
                sumX += p.x();
                sumY += p.y();
                sumZ += p.z();
            }
            double centerX = sumX / layoutResults.length;
            double centerY = sumY / layoutResults.length;
            double centerZ = sumZ / layoutResults.length;

            // Place hologram a few blocks above the average element Y (adjust offset as needed)
            hologram = new HologramDisplay(instance, new Pos(centerX, centerY + 5.5, centerZ));
        } else {
            // Fallback: place hologram relative to origin
            hologram = new HologramDisplay(instance, origin.add(8, 5, 0));
        }
    }

    @Override
    public void cleanUp() {
        // Despawn/remove everything owned by this Scene
        for (var display : displaysBySlot.values()) {
            safeRemove(display);
        }
        if (hologram != null) {
            hologram.remove();
            hologram = null;
        }
        clearGlowing();
        displaysBySlot.clear();
        slotStates.clear();
        started = false;
    }

    @Override
    public void setValue(int slot, int value) {
        assertStarted();
        var display = requireDisplay(slot);
        display.setValue(value);
    }

    @Override
    public void setHighlighted(int slot, boolean highlighted) {
        assertStarted();
        var display = requireDisplay(slot);

        highlightedSlots.add(slot);
        display.setGlowing(highlighted);
    }

    @Override
    public void clearGlowing() {
        // Turn off highlight visuals for all currently highlighted slots
        for (int slot : new HashSet<>(highlightedSlots)) {
            var display = displaysBySlot.get(slot);
            if (display != null) {
                display.setGlowing(false);
            }
        }
        highlightedSlots.clear();
    }

    @Override
    public void moveSlotTo(int slot, Pos pos) {
        assertStarted();
        var display = requireDisplay(slot);
        display.teleport(pos);
    }

    @Override
    public void swapSlots(int a, int b) {
        assertStarted();

        var da = requireDisplay(a);
        var db = requireDisplay(b);

        displaysBySlot.put(a, db);
        displaysBySlot.put(b, da);

        var posA = da.getPos();
        var posB = db.getPos();

        da.teleport(posB);
        db.teleport(posA);
    }

    @Override
    public void playSound(String key, float volume, float pitch) {
        assertStarted();
        audience.playSound(key, volume, pitch);
    }


    @Override
    public void sendMessage(Component message) {
        audience.sendMessage(message);
    }

    @Override
    public void showHologram(Component text) {
        assertStarted();
        if (hologram != null) {
            hologram.setText(text);
        }
    }

    public void hoverDisplay(int slot, boolean hover) {
        assertStarted();

        var dv = requireDisplay(slot);
        if (hover) {
            dv.teleport(dv.getPos().add(0, 1, 0));
        } else {
            dv.teleport(dv.getPos().add(0, -1, 0));
        }
    }

    @Override
    public void toggleCellState(int slot, CellState first, CellState second) {
        assertStarted();
        var current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        var next = current == first ? second : first;
        slotStates.put(slot, next);
        applyCellState(slot, next);
    }

    @Override
    public void stopAnimations() {
        clearGlowing();
        clearHologram();
        // SystemMessages.sendTo(audience, SystemMessages.ALGORITHM_COMPLETE);
    }

    // -------------------------
    // Internals
    // -------------------------

    private IDisplayValue requireDisplay(int slot) {
        var display = displaysBySlot.get(slot);
        if (display == null) {
            throw new IllegalStateException("No display for slot " + slot + ".");
        }
        return display;
    }

    private void safeRemove(IDisplayValue display) {
        if (display == null) return;
        try {
            display.remove();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void assertStarted() {
        if (!started) {
            throw new IllegalStateException("Scene not started. Call onStart() before using SceneOps.");
        }
    }

    private void applyCellState(int slot, CellState state) {
        var display = requireDisplay(slot);
        if (!(display instanceof IBlockStateDisplay blockDisplay)) {
            return;
        }
        blockDisplay.setBlock(blockForState(state));
    }

    private static CellState initialCellState(Object value) {
        if (!(value instanceof Integer number)) {
            return CellState.DEFAULT;
        }
        return switch (number) {
            case 1 -> CellState.WALL;
            case 2 -> CellState.START;
            case 3 -> CellState.GOAL;
            default -> CellState.DEFAULT;
        };
    }

    private static Block blockForState(CellState state) {
        return switch (state) {
            case DEFAULT -> Block.SMOOTH_STONE;
            case WALL -> Block.BLACK_CONCRETE;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.GREEN_CONCRETE;
            case PATH -> Block.ORANGE_CONCRETE;
        };
    }

    private <T extends Comparable<T>> boolean isAStarGrid(LayoutResult<T>[] layoutResults) {
        if (layoutResults.length == 0) return false;
        for (var layoutResult : layoutResults) {
            Object raw = layoutResult.value().value();
            if (!(raw instanceof Integer number)) return false;
            if (number < 0 || number > 3) return false;
        }
        return true;
    }
}
