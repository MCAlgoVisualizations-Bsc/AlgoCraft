package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public record CircleLayout(double radius, double yOffset) implements ILayout {

    public CircleLayout() {
        this(2.0, 2.0);
    }
    public CircleLayout (double radius) {
        this(radius, 2.0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin) {
        var size = model.size();
        var out = new LayoutResult[size];

        double y = origin.y() + yOffset;

        if (size == 0) return new LayoutResult[0];

        if (size == 1) {
            out[0] = new LayoutResult<>(model.getFirst(), origin);
            return out;
        }
 
        for (int i = 0; i < size; i++) {
            double angle = (2.0 * Math.PI * i) / size;
            double x = origin.x() + (Math.cos(angle) * radius);
            double z = origin.z() + (Math.sin(angle) * radius);
            final var pos = new Pos(x, y, z);
            out[i] = new LayoutResult<>(model.get(i), pos);
        }

        return out;
    }

}
