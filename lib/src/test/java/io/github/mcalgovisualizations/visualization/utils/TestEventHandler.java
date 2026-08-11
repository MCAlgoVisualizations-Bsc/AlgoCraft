package io.github.mcalgovisualizations.visualization.utils;

import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class TestEventHandler implements IAnimationHandler<TestEvent> {
    @Override
    @SuppressWarnings("unchecked")
    public AnimationPlan<ISceneOps> handle(TestEvent event) {
        return AnimationPlan.empty();
    }
}