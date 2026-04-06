package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;

/**
 * Places items by simulating a Binary Search Tree insertion to determine logical
 * parent/child relationships, then assigns coordinates based on tree depth.
 */
public record BSTNodeLayout(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing,
        double zOffset
) implements ILayout {

    public BSTNodeLayout() {
        this(4.0, 2.0, 0.5, 0.0);
    }

    public BSTNodeLayout {
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
        LayoutResult<T>[] out = new LayoutResult[size];

        // 1. Build the logical tree
        Node<T> root = new Node<>(model.get(0), 0);
        for (int i = 1; i < size; i++) {
            insert(root, model.get(i), i);
        }

        // 2. Find maximum depth to calculate horizontal scale
        int maxDepth = getMaxDepth(root);

        // 3. Assign physical positions
        assignPositions(root, origin, 0, 0.0, maxDepth, out);

        return out;
    }

    // --- Tree Logic ---

    private <T extends Comparable<T>> void insert(Node<T> root, Data<T> data, int index) {
        Node<T> current = root;
        T candidate = data.value();

        while (true) {
            T currentValue = current.data.value();
            if (candidate.compareTo(currentValue) < 0) {
                if (current.left == null) {
                    current.left = new Node<>(data, index);
                    break;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node<>(data, index);
                    break;
                }
                current = current.right;
            }
        }
    }

    private <T extends Comparable<T>> int getMaxDepth(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(getMaxDepth(node.left), getMaxDepth(node.right));
    }

    private <T extends Comparable<T>> void assignPositions(Node<T> node, Pos origin, int depth, double xOffset, int maxDepth, LayoutResult<T>[] out) {
        if (node == null) return;

        double x = origin.x() + xOffset;
        double y = origin.y() + rootYOffset - (depth * levelDrop);
        double z = origin.z() + zOffset;

        BstNodeStylingProfile.NodeRole role = depth == 0
                ? BstNodeStylingProfile.NodeRole.ROOT
                : (node.left == null && node.right == null
                ? BstNodeStylingProfile.NodeRole.LEAF
                : BstNodeStylingProfile.NodeRole.INTERNAL);

        double widthScale = Math.pow(2, Math.max(0, 4 - depth));
        double step = Math.max(1.25, horizontalSpacing * widthScale);

        Pos leftPos = node.left != null ? buildPosition(origin, depth + 1, xOffset - step) : null;
        Pos rightPos = node.right != null ? buildPosition(origin, depth + 1, xOffset + step) : null;

        out[node.originalIndex] = new LayoutResult<>(
                node.data,
                new Pos(x, y, z),
                new ParticleTreeNodeStylingProfile(role, leftPos, rightPos)
        );

        assignPositions(node.left, origin, depth + 1, xOffset - step, maxDepth, out);
        assignPositions(node.right, origin, depth + 1, xOffset + step, maxDepth, out);
    }

    private Pos buildPosition(Pos origin, int depth, double xOffset) {
        return new Pos(
                origin.x() + xOffset,
                origin.y() + rootYOffset - (depth * levelDrop),
                origin.z() + zOffset
        );
    }

    // --- Inner Helper Class ---

    private static class Node<T extends Comparable<T>> {
        final Data<T> data;
        final int originalIndex; // Remembers where it goes in the output array!
        Node<T> left;
        Node<T> right;

        Node(Data<T> data, int originalIndex) {
            this.data = data;
            this.originalIndex = originalIndex;
        }
    }
}