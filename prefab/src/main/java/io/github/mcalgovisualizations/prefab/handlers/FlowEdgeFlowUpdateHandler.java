package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.events.FlowEdgeFlowUpdate;
import io.github.mcalgovisualizations.prefab.scenes.FlowScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

@Deprecated
public final class FlowEdgeFlowUpdateHandler implements IAnimationHandler<FlowEdgeFlowUpdate> {
    @Override
    public AnimationPlan<FlowScene> handle(FlowEdgeFlowUpdate event) {
        return AnimationPlan.instant(sceneOps -> sceneOps.setValue(event.slot(), event.currentFlow()));
    }
}

