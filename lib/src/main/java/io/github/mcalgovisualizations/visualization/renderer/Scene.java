package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.Displays.MobDisplay;
import io.github.mcalgovisualizations.visualization.renderer.handlers.SystemMessages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Scene = Minestom world state.
 *
 */
public final class Scene implements ISceneOps {

    private final Instance instance;
    private final Pos origin;
    public final List<Player> viewers = new ArrayList<>();
    private Audience audience = Audience.empty();

    public void setAudience(Audience audience) {
        this.audience = Objects.requireNonNullElse(audience, Audience.empty());
    }

    // Stable identity mapping (slot -> display wrapper/entity)
    private final Map<Integer, IDisplayValue> displaysBySlot =
            new HashMap<>();

    // Floating hologram above the visualization
    private HologramDisplay hologram;

    // Visual state
    private final Set<Integer> highlightedSlots = new HashSet<>();

    private boolean started = false;

    public Scene(@NotNull Instance instance, @NotNull Pos origin) {
        this.instance = instance;
        this.origin = origin;
    }

    @Override
    public <T extends Comparable<T>> void onStart(LayoutResult<T>[] layoutResults) {
        this.started = true;

        // Rank values 1–10 across the mob ladder regardless of the actual type (Integer, String, etc.)
        List<T> sorted = Arrays.stream(layoutResults)
                .map(r -> r.value().value())
                .distinct()
                .sorted()
                .toList();
        int uniqueCount = sorted.size();

        for(int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            int rank0 = sorted.indexOf(value.value());
            int mobValue = Math.clamp(
                    (int) Math.round((rank0 / (double) Math.max(uniqueCount - 1, 1)) * 9) + 1,
                    1, 10
            );

            // TODO : Move the creation of IDisplayValue somewhere else
            var dv = new MobDisplay(instance, pos, mobValue, value.toString());
            //var dv = new BlockDisplay(instance, pos, Block.GRANITE, value.toString());

            displaysBySlot.put(i, dv);

            dv.setInstance();
        }

        // Create hologram dynamicallyy floating above the center of the layout
        if (layoutResults != null && layoutResults.length > 0) {
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

        // Add viewers after all displays have been created
        displaysBySlot.values().forEach(display -> viewers.forEach(display::addViewer));
        viewers.forEach(hologram::addViewer);
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
        assertStarted();

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
    public void playEffect(int slot, String effectId) {
        assertStarted();

        viewers.forEach(viewer -> viewer.playSound(Sound.sound(
                Key.key("minecraft:block.note_block.pling"), Sound.Source.MASTER, 1.0f, 1.0f
        )));
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
    public void stopAnimations() {
        clearGlowing();
        clearHologram();
        SystemMessages.sendTo(audience, SystemMessages.ALGORITHM_COMPLETE);
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
}
