package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

@FunctionalInterface
public interface IAnimationHandler<E extends IAlgorithmEvent> {
    <O extends ISceneOps> AnimationPlan<O> handle(E event);
}
