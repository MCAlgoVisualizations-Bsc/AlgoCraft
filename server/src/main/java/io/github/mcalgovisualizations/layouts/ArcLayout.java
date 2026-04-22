package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;

import java.util.List;

public record ArcLayout(
        double radius,
        double yOffset,
        double startAngle,
        double sweepAngle,
        boolean closed
) implements ILayout<List<Integer>> {

    public ArcLayout() {
        this(6.0, 2.0, -Math.PI / 2, 3 * Math.PI / 2, false);
    }

    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }
        int size = model.size();
        LayoutResult[] out = new LayoutResult[size];
        double y = origin.y() + yOffset;

        if (size == 1) {
            double x = origin.x() + Math.cos(startAngle) * radius;
            double z = origin.z() + Math.sin(startAngle) * radius;
            Pos pos = new Pos(x, y, z);
            out[0] = new LayoutResult(
                    model.getFirst(),
                    pos,
                    new EntityCreatureDisplay(pos, EntityType.VILLAGER, Integer.toString(model.getFirst())));
            return out;
        }

        int divisor = closed ? size : size - 1;
        double step = sweepAngle / divisor;

        for (int i = 0; i < size; i++) {
            double angle = startAngle + step * i;
            double x = origin.x() + Math.cos(angle) * radius;
            double z = origin.z() + Math.sin(angle) * radius;

            Pos pos = new Pos(x, y, z);
            out[i] = new LayoutResult(model.get(i), pos, new EntityCreatureDisplay(pos, EntityType.VILLAGER, Integer.toString(model.get(i))));
        }

        return out;
    }
}
