package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.events.FlowEdgeFlowUpdate;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class FlowEdgeFlowUpdateHandler implements IAnimationHandler<FlowEdgeFlowUpdate> {
    @Override
    public AnimationPlan<ISceneOps> handle(FlowEdgeFlowUpdate event) {
        return AnimationPlan.instant(sceneOps -> sceneOps.setValue(event.slot(), event.currentFlow()));
    }
}

