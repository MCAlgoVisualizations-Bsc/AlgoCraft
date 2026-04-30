package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.GridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.minestom.server.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public final class CellStateTransitionHandler implements IAnimationHandler<CellStateTransition> {

    private static final int SEARCHER_SLOT = -100;

    @Override
    public AnimationPlan<GridScene> handle(CellStateTransition event) {
        return AnimationPlan.<GridScene>builder()
                .stepAsync(1, sceneOps -> {
                    var searcher = sceneOps.getDisplay(SEARCHER_SLOT);
                    
                    // Toggle the state of the block
                    sceneOps.toggleCellState(event.slot(), event.first(), event.second());

                    if (searcher == null) {
                        // Spawn at the target slot
                        var targetDisplay = sceneOps.getDisplay(event.slot());
                        if (targetDisplay != null) {
                            var startPos = targetDisplay.getPos();
                            sceneOps.addDisplay(SEARCHER_SLOT, new EntityCreatureDisplay(startPos, EntityType.VILLAGER, "Searcher", true));
                        }
                        return CompletableFuture.completedFuture(null);
                    } else {
                        // Walk to the target slot
                        var targetDisplay = sceneOps.getDisplay(event.slot());
                        if (targetDisplay != null) {
                            return sceneOps.walkSlotTo(SEARCHER_SLOT, targetDisplay.getPos());
                        }
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .build();
    }
}
