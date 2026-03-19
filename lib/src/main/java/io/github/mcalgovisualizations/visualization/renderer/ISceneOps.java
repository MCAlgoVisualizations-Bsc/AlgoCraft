package io.github.mcalgovisualizations.visualization.renderer;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;

public interface ISceneOps {

    // lifecycle
    <T extends Comparable<T>> void onStart(LayoutResult<T>[] model);
    void cleanUp();

    // state
    void setValue(int slot, int value);

    void setHighlighted(int slot, boolean highlighted);

    void clearGlowing();

    // movement primitives
    void moveSlotTo(int slot, Pos position);

    void swapSlots(int a, int b);

    // optional extension point
    void playEffect(int slot, String effectId);

    void sendMessage(Component message);

    void hoverDisplay(int slot, boolean hover);

    /**
     * Show a floating hologram text above the visualization.
     * Pass {@code Component.empty()} to clear it.
     */
    void showHologram(Component text);

    /** Clear the hologram text. */
    default void clearHologram() {
        showHologram(Component.empty());
    }

    void stopAnimations();

}

