package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

@FunctionalInterface
public interface IAnimationHandler<E extends IAlgorithmEvent> {
    AnimationPlan handle(E event);
}
