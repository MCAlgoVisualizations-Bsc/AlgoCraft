package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

@FunctionalInterface
public interface IAnimationHandler<E extends IAlgorithmEvent> {
    <O extends ISceneOps> AnimationPlan<O> handle(E event);
}
