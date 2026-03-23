package io.github.mcalgovisualizations.visualization.renderer.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

@FunctionalInterface
public interface IAnimationHandler<E extends IAlgorithmEvent> {
    AnimationPlan handle(E event);
}
