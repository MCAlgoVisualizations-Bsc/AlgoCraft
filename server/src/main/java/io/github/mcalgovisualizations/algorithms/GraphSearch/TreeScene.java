package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TreeScene extends AbstractScene {
    public TreeScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int i = 0; i < layoutResults.length; i++) {
            addDisplay(i, layoutResults[i].displayValue());
        }
    }

    public CompletableFuture<Void> walkSlotTo(int slot, Pos pos) {
        var display = (EntityCreatureDisplay) requireDisplay(slot);
        return display.walkTo(pos);
    }

    public @Nullable IDisplayValue getDisplay(int slot) {
        return displaysBySlot.get(slot);
    }
}
