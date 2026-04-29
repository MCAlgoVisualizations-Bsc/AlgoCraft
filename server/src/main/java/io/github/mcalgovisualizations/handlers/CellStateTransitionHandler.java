package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.CaveTunnelScene;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.GridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class CellStateTransitionHandler implements IAnimationHandler<CellStateTransition> {
    @Override
    public <O extends ISceneOps> AnimationPlan<O> handle(CellStateTransition event) {
        return AnimationPlan.instant(sceneOps -> {
            if (sceneOps instanceof GridScene gridScene) {
                gridScene.toggleCellState(event.slot(), event.first(), event.second());
            } else if (sceneOps instanceof CaveTunnelScene caveScene) {
                caveScene.toggleCellState(event.slot(), event.first(), event.second());
            }
        });
    }
}

