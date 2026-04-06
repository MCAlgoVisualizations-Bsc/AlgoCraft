package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.layouts.BstNodeStylingProfile;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import java.util.List;

public record UnorderedTreeLayout(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing
) implements ILayout {

    public UnorderedTreeLayout() {
        this(10.0, 3.0, 2.0);
    }

    @Override
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin, Instance instance) {
        int size = model.size();
        LayoutResult<T>[] out = new LayoutResult[size];

        for (int i = 0; i < size; i++) {
            // Depth in a complete binary tree: floor(log2(i + 1))
            int depth = (int) (Math.log(i + 1) / Math.log(2));

            // Position within the current horizontal row
            int firstIndexInLevel = (int) Math.pow(2, depth) - 1;
            int posInLevel = i - firstIndexInLevel;
            int nodesInLevel = (int) Math.pow(2, depth);

            // Center nodes horizontally
            double xOffset = (posInLevel - (nodesInLevel - 1) / 2.0) * (horizontalSpacing * Math.pow(2, 3 - depth));

            double x = origin.x() + xOffset;
            double y = origin.y() + rootYOffset - (depth * levelDrop);
            double z = origin.z();

            // Style based on role
            BstNodeStylingProfile.NodeRole role = (i == 0) ? BstNodeStylingProfile.NodeRole.ROOT : BstNodeStylingProfile.NodeRole.INTERNAL;

            out[i] = new LayoutResult<>(model.get(i), new Pos(x, y, z), new BstNodeStylingProfile(role));
        }
        return out;
    }
}