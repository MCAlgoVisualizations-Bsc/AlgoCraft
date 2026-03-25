package io.github.mcalgovisualizations.visualization.renderer.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

public final class CellStateTransitionHandler implements IAnimationHandler<CellStateTransition> {
    @Override
    public AnimationPlan handle(CellStateTransition event) {
        return AnimationPlan.instant(sceneOps ->
                sceneOps.toggleCellState(event.slot(), event.first(), event.second())
        );
    }
}

