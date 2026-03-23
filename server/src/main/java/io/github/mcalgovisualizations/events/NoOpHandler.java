package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

public class NoOpHandler implements IAnimationHandler<NoOp> {
    @Override
    public AnimationPlan handle(NoOp event) {
        return AnimationPlan.empty();
    }
}
