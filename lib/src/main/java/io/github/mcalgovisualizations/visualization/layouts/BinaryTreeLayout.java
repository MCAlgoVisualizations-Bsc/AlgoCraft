package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;

/**
 * Places items as a complete binary tree using their list index as heap index.
 */
public record BinaryTreeLayout(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing,
        double zOffset
) implements ILayout {

    public BinaryTreeLayout() {
        this(4.0, 2.0, 1.8, 0.0);
    }

    public BinaryTreeLayout {
        if (levelDrop <= 0) throw new IllegalArgumentException("levelDrop must be > 0");
        if (horizontalSpacing <= 0) throw new IllegalArgumentException("horizontalSpacing must be > 0");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        int size = model.size();
        int maxLevel = levelOf(size - 1);
        var out = new LayoutResult[size];

        for (int i = 0; i < size; i++) {
            int level = levelOf(i);
            int nodesBeforeLevel = (1 << level) - 1;
            int indexInLevel = i - nodesBeforeLevel;
            int nodesInLevel = 1 << level;

            double center = (nodesInLevel - 1) / 2.0;
            double widthScale = Math.pow(2, Math.max(0, maxLevel - level));

            double x = origin.x() + ((indexInLevel - center) * horizontalSpacing * widthScale);
            double y = origin.y() + rootYOffset - (level * levelDrop);
            double z = origin.z() + zOffset;

            out[i] = new LayoutResult<>(model.get(i), new Pos(x, y, z), new StylingProfile());
        }

        return out;
    }

    private static int levelOf(int index) {
        return 31 - Integer.numberOfLeadingZeros(index + 1);
    }
}

