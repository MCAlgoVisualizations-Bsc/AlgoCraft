package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.algorithms.mazes.Scenes.GridScene;
import io.github.mcalgovisualizations.algorithms.mazes.Scenes.HeuristicGridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.minestom.server.entity.EntityType;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.CompletableFuture;

public final class CellStateTransitionHandler implements IAnimationHandler<CellStateTransition> {

    @Override
    public AnimationPlan<GridScene> handle(CellStateTransition event) {
        return AnimationPlan.<GridScene>builder()
                .stepAsync(1, scene -> {
                    scene.toggleCellState(event.slot(), event.first(), event.second());
                    scene.moveVillager(event.slot());

                    //Check if this is a heuristic scene and update the locator bar if so
                    if (scene instanceof HeuristicGridScene heuristicScene) {
                        heuristicScene.updateLocatorBar(event.slot());
                    }

                    return CompletableFuture.completedFuture(null);
                })
                .build();
    }
}
