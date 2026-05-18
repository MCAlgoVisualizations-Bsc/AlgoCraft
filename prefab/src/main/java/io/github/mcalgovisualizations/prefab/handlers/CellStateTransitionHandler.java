package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.events.CellStateTransition;
import io.github.mcalgovisualizations.prefab.algorithms.mazes.Scenes.GridScene;
import io.github.mcalgovisualizations.prefab.algorithms.mazes.Scenes.HeuristicGridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

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
