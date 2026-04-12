package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;

/**
 * Deterministic row-major 2D grid layout.
 */
public record GridLayout(
        int columns,
        double spacing,
        double yOffset,
        double zOffset
) implements ILayout<List<Integer>> {

    public GridLayout(int columns) {
        this(columns, 1.5, 0.0, 0.0);
    }

    public GridLayout {
        if (columns <= 0) throw new IllegalArgumentException("columns must be > 0");
        if (spacing <= 0) throw new IllegalArgumentException("spacing must be > 0");
    }

    @SuppressWarnings("unchecked")
    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        var out = new LayoutResult[model.size()];
        double y = origin.y() + yOffset;
        double zBase = origin.z() + zOffset;

        for (int idx = 0; idx < model.size(); idx++) {
            int row = idx / columns;
            int col = idx % columns;

            double x = origin.x() + (col * spacing);
            double z = zBase + (row * spacing);
            out[idx] = new LayoutResult(model.get(idx), new Pos(x, y, z), new StylingProfile());
        }

        return out;
    }
}

