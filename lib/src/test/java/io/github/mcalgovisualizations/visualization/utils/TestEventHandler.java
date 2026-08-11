package io.github.mcalgovisualizations.visualization.utils;

import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class TestEventHandler implements IAnimationHandler<FirstTestEvent> {
    @Override
    @SuppressWarnings("unchecked")
    public AnimationPlan<ISceneOps> handle(FirstTestEvent event) {
        return AnimationPlan.empty();
    }
}