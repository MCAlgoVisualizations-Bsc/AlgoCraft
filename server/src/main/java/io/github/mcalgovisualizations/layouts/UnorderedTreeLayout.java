package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
@Deprecated
public record UnorderedTreeLayout<T extends Comparable<T>>(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing
) implements ILayout<List<T>> {

    public UnorderedTreeLayout() {
        this(10.0, 3.0, 2.0);
    }

    @Override
    public LayoutResult[] compute(List<T> model, Pos origin, Instance instance) {
        int size = model.size();
        LayoutResult[] out = new LayoutResult[size];
        Pos[] positions = new Pos[size];

        for (int i = 0; i < size; i++) {
            int depth = (int) (Math.log(i + 1) / Math.log(2));
            int firstIndexInLevel = (int) Math.pow(2, depth) - 1;
            int posInLevel = i - firstIndexInLevel;
            int nodesInLevel = (int) Math.pow(2, depth);

            double xOffset = (posInLevel - (nodesInLevel - 1) / 2.0) * (horizontalSpacing * Math.pow(2, 3 - depth));
            double x = origin.x() + xOffset;
            double y = origin.y() + rootYOffset - (depth * levelDrop);
            double z = origin.z();

            positions[i] = new Pos(x, y, z);
        }

        for (int i = 0; i < size; i++) {
            int leftChild = (2 * i) + 1;
            int rightChild = (2 * i) + 2;

            Pos leftPos = leftChild < size ? positions[leftChild] : null;
            Pos rightPos = rightChild < size ? positions[rightChild] : null;

            BstNodeStylingProfile.NodeRole role = i == 0
                    ? BstNodeStylingProfile.NodeRole.ROOT
                    : (leftPos == null && rightPos == null
                    ? BstNodeStylingProfile.NodeRole.LEAF
                    : BstNodeStylingProfile.NodeRole.INTERNAL);

            out[i] = new LayoutResult(
                    model.get(i),
                    positions[i],
                    new ParticleTreeNodeStylingProfile(role, leftPos, rightPos).applyStyle(model.get(i).toString(), positions[i])
            );
        }

        return out;
    }
}