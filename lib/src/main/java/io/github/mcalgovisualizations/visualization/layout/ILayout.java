package io.github.mcalgovisualizations.visualization.layout;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

/**
 * Defines a strategy for transforming a model into a set of positioned display elements.
 *
 * <p>The layout is responsible for mapping the input model {@code <I>} into one
 * {@link LayoutResult} instances, each describing where and how something should be rendered
 * in the world.</p>
 *
 * <p>The implementation should be deterministic for a given input and must not mutate the model nor set instances.</p>
 *
 * @param <I> the type of the input model to layout
 */
public interface ILayout<I> {

    /**
     * Computes the spatial layout for the given model.
     *
     * @param model the input data to be transformed into display elements
     * @param origin the base position used as a reference point for the layout
     * @param instance the target instance where the layout will be applied / useful for checking in-game bounds
     * @return an array of {@link LayoutResult}, each representing a positioned display element
     *
     * Implementations should:
     * <ul>
     *     <li>Return a non-null array (empty if nothing to render)</li>
     *     <li>Avoid side effects (do not spawn entities directly)</li>
     *     <li>Use {@code origin} as the anchor for all positioning</li>
     * </ul>
     */
    LayoutResult[] compute(I model, Pos origin, Instance instance);
}