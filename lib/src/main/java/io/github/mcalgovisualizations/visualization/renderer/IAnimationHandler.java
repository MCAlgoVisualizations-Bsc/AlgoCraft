package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

/**
 * Converts a concrete algorithm event into an animation plan for a scene.
 *
 * @param <E> event type handled by this animation handler
 */
@FunctionalInterface
public interface IAnimationHandler<E extends IAlgorithmEvent> {
    /**
     * Builds the animation plan for the given event.
     *
     * @param event the event to convert
     * @param <O> concrete scene type
     * @return the animation plan for the event
     */
    <O extends ISceneOps> AnimationPlan<O> handle(E event);
}
