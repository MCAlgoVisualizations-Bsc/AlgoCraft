package io.github.mcalgovisualizations.visualization.renderer.scene;

import com.google.common.collect.Maps;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractScene implements ISceneOps {

    protected final Map<Integer, IDisplayValue> displaysBySlot = new HashMap<>();
    protected final Set<Integer> highlightedSlots = new HashSet<>();

    protected final Instance instance;
    protected final AudienceChannel audience;
    protected final Pos origin;

    public AbstractScene(@NotNull SceneContext context) {
        this.instance = context.instance();
        this.audience = context.audience();
        this.origin = context.origin();
    }

    public abstract void setLayout(LayoutResult[] layoutResults);

    @Override
    public Pos getOrigin() {
        return this.origin;
    }

    @Override
    public void setValue(int slot, int value) {
        System.err.println("Not sure this should be in the API");
    }

    @Override
    public void setHighlighted(int slot, boolean highlighted) {
        var dv = requireDisplay(slot);
        dv.setGlowing(highlighted);

        if (highlighted) {
            highlightedSlots.add(slot);
        } else {
            highlightedSlots.remove(slot);
        }
    }

    @Override
    public void clearGlowing() {
        this.highlightedSlots.forEach(slot -> requireDisplay(slot).setGlowing(false));
        this.highlightedSlots.clear();
    }

    @Override
    public void hoverDisplay(int slot, boolean hover) {
        var dv = requireDisplay(slot);
        if (hover) {
            dv.teleport(dv.getPos().add(0, 1, 0));
        } else {
            dv.teleport(dv.getPos().add(0, -1, 0));
        }
    }

    @Override
    public void moveSlotTo(int slot, Pos pos) {
        var display = requireDisplay(slot);
        display.teleport(pos);
    }

    @Override
    public void swapSlots(int a, int b) {
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
    public void playSound(@NotNull String key, float volume, float pitch) {
        try{
            this.audience.playSound(key, volume, pitch);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public Map<Integer, IDisplayValue> getDisplaySlots(Class<? extends IDisplayValue> cls) {
        return Maps.filterEntries(this.displaysBySlot, e -> {
            assert e.getValue() != null;
            return cls.isAssignableFrom(e.getValue().getClass());
        });
    }

    @Override
    public void addDisplay(int slot, IDisplayValue display) {
        var dv = displaysBySlot.get(slot);
        if (dv != null) {
            throw new IllegalArgumentException("Slot already has a display");
        }
        displaysBySlot.put(slot, display);
        display.setInstance(instance);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        audience.sendMessage(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        this.audience.sendActionBar(message);
    }

    @Override
    public void cleanUp() {
        for (var display : displaysBySlot.values()) {
            safeRemove(display);
        }

        clearGlowing();
        displaysBySlot.clear();
    }

    // -------------------------
    // Internals
    // -------------------------
    protected IDisplayValue requireDisplay(int slot) {
        var display = displaysBySlot.get(slot);
        if (display == null) {
            throw new IllegalStateException("No display for slot " + slot + ".");
        }
        return display;
    }

    private void safeRemove(@NotNull IDisplayValue display) {
        try {
            display.remove();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
