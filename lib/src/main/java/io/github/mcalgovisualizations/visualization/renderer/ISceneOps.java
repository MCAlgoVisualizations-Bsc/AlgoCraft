package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.Map;

public interface ISceneOps {

    Map<Integer, IDisplayValue> displaysBySlot = new HashMap<>();

    void setLayout(LayoutResult[] model);

    Pos getOrigin();

    void setValue(int slot, int value);
    void setHighlighted(int slot, boolean highlighted);
    void clearGlowing();
    void hoverDisplay(int slot, boolean hover);

    void moveSlotTo(int slot, Pos position);
    void swapSlots(int a, int b);

    void playSound(String key, float volume, float pitch);
    void sendMessage(Component message);
    void sendActionBar(Component message);
    void toggleCellState(int slot, CellState first, CellState second);

    void cleanUp();
}

