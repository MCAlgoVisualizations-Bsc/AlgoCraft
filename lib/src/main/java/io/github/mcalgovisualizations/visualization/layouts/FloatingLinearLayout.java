package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;

import java.util.List;

/**
 * A {@link ILayout} implementation that arranges elements in a straight
 * horizontal line along the positive X-axis.
 * <p>
 * All elements share a constant Y-coordinate ("floating"), meaning their
 * vertical position does not depend on the element value.
 * <p>
 * Elements are spaced evenly using the configured {@code spacing}.
 *
 * @param spacing distance in blocks between consecutive elements (must be > 0)
 * @param yOffset vertical offset added to {@code origin.y()}
 * @param zOffset depth offset added to {@code origin.z()}
 */
public record FloatingLinearLayout(
        double spacing,
        double yOffset,
        double zOffset
) implements ILayout {

    /**
     * Creates a floating linear layout with default configuration:
     * <ul>
     *     <li>spacing = 2.0</li>
     *     <li>yOffset = 0.0</li>
     *     <li>zOffset = 0.0</li>
     * </ul>
     * Useful for testing
     */
    public FloatingLinearLayout() { this(2.0, 0.0, 0.0); }

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if {@code spacing <= 0}
     */
    public FloatingLinearLayout {
        if (spacing <= 0) {
            throw new IllegalArgumentException("spacing must be > 0");
        }
    }

    /**
     * Computes a linear layout for {@code size} elements.
     *
     * @param origin starting position of the layout
     * @return array of {@link Pos} positions in a straight line
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin) {
        if(model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        final var size = model.size();
        final var out = new LayoutResult[size];

        final double y = origin.y() + yOffset;
        final double z = origin.z() + zOffset;

        for (int i = 0; i < size; i++) {
            final double x = origin.x() + (i * spacing);
            final var pos = new Pos(x, y, z);

            out[i] = new LayoutResult<>(model.get(i), pos);
        }

        return out;
    }
}
