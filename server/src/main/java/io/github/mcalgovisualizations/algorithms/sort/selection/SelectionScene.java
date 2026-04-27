package io.github.mcalgovisualizations.algorithms.sort.selection;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SelectionScene extends AbstractScene {
    private EntityCreatureDisplay golemTracker;


    public SelectionScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for(int i = 0; i < layoutResults.length; i++) {
            final var dv = (EntityCreatureDisplay) layoutResults[i].displayValue();
            displaysBySlot.put(i, dv);
            dv.setInstance(instance);
            dv.lookAt(origin);
        }

        golemTracker = new EntityCreatureDisplay(origin.add(0, -1, 0), EntityType.COPPER_GOLEM, "Golem");
        golemTracker.setInstance(instance);

        golemTracker.goTo(displaysBySlot.get(0).getPos());
    }


}
