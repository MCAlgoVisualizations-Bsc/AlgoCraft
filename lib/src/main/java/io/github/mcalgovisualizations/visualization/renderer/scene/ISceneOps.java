package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface ISceneOps {

    void setLayout(LayoutResult[] model);

    Pos getOrigin();

    void setHighlighted(int slot, boolean highlighted);
    void clearGlowing();
    void hoverDisplay(int slot, boolean hover);

    void moveSlotTo(int slot, Pos position);
    void swapSlots(int a, int b);
    void addDisplay(int slot, IDisplayValue display);

    void playSound(String key, float volume, float pitch);
    void sendMessage(Component message);
    void sendActionBar(Component message);

    void cleanUp();
}
