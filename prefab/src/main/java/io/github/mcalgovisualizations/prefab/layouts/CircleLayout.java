package io.github.mcalgovisualizations.prefab.layouts;

import io.github.mcalgovisualizations.prefab.Displays.MobDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
@Deprecated
public record CircleLayout(double radius, double yOffset) implements ILayout<List<Integer>> {

    public CircleLayout() {
        this(2.0, 2.0);
    }

    public CircleLayout(double radius) {
        this(radius, 2.0);
    }

    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        var size = model.size();

        var out = new LayoutResult[size];
        double y = origin.y() + yOffset;

        if (size == 1) {
            out[0] = new LayoutResult(model.getFirst(), origin, new MobDisplay(origin, model.getFirst().toString()));
            return out;
        }

        for (int i = 0; i < size; i++) {
            double angle = (2.0 * Math.PI * i) / size;
            double x = origin.x() + (Math.cos(angle) * radius);
            double z = origin.z() + (Math.sin(angle) * radius);

            final var pos = new Pos(x, y, z);
            out[i] = new LayoutResult(model.get(i), pos, new MobDisplay(pos, model.get(i).toString()));

        }

        return out;
    }
}
