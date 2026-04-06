package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.renderer.GridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

public final class CellStateTransitionHandler implements IAnimationHandler<CellStateTransition> {
    @Override
    public AnimationPlan<GridScene> handle(CellStateTransition event) {
        return AnimationPlan.instant(sceneOps ->
                sceneOps.toggleCellState(event.slot(), event.first(), event.second())
        );
    }
}

