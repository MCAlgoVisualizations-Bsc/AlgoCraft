package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;

/**
 * Places values by simulating a ternary search tree insertion to determine logical
 * parent/left/middle/right relationships, then assigns coordinates based on tree depth.
 */
public record TSTNodeLayout<T extends Comparable<T>>(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing,
        double zOffset
) implements ILayout<List<T>> {

    public TSTNodeLayout() {
        this(4.0, 2.0, 0.5, 0.0);
    }

    public TSTNodeLayout {
        if (levelDrop <= 0) throw new IllegalArgumentException("levelDrop must be > 0");
        if (horizontalSpacing <= 0) throw new IllegalArgumentException("horizontalSpacing must be > 0");
    }

    @Override
    public LayoutResult[] compute(List<T> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        int size = model.size();
        LayoutResult[] out = new LayoutResult[size];

        Node<T> root = new Node<>(model.getFirst(), 0);
        for (int i = 1; i < size; i++) {
            insert(root, model.get(i), i);
        }

        int maxDepth = getMaxDepth(root);
        assignPositions(root, origin, 0, 0.0, maxDepth, out);
        return out;
    }

    private void insert(Node<T> root, T data, int index) {
        Node<T> current = root;

        while (true) {
            var currentValue = current.data;
            int cmp = data.compareTo(currentValue);
            if (cmp < 0) {
                if (current.left == null) {
                    current.left = new Node<>(data, index);
                    break;
                }
                current = current.left;
            } else if (cmp > 0) {
                if (current.right == null) {
                    current.right = new Node<>(data, index);
                    break;
                }
                current = current.right;
            } else {
                if (current.middle == null) {
                    current.middle = new Node<T>(data, index);
                    break;
                }
                current = current.middle;
            }
        }
    }

    private int getMaxDepth(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(node.left == null ? 0 : getMaxDepth(node.left),
                Math.max(node.middle == null ? 0 : getMaxDepth(node.middle),
                        node.right == null ? 0 : getMaxDepth(node.right)));
    }

    private void assignPositions(Node<T> node, Pos origin, int depth, double xOffset, int maxDepth, LayoutResult[] out) {
        if (node == null) return;

        double x = origin.x() + xOffset;
        double y = origin.y() + rootYOffset - (depth * levelDrop);
        double z = origin.z() + zOffset + (depth * 0.45);
        Pos currentPos = new Pos(x, y, z);

        BstNodeStylingProfile.NodeRole role = depth == 0
                ? BstNodeStylingProfile.NodeRole.ROOT
                : (node.left == null && node.middle == null && node.right == null
                ? BstNodeStylingProfile.NodeRole.LEAF
                : BstNodeStylingProfile.NodeRole.INTERNAL);

        double widthScale = Math.pow(2, Math.max(0, Math.min(4, maxDepth - depth)));
        double step = Math.max(1.25, horizontalSpacing * widthScale);

        Pos leftPos = node.left != null ? buildPosition(origin, depth + 1, xOffset - step) : null;
        Pos middlePos = node.middle != null ? buildPosition(origin, depth + 1, xOffset) : null;
        Pos rightPos = node.right != null ? buildPosition(origin, depth + 1, xOffset + step) : null;

        out[node.originalIndex] = new LayoutResult(
                node.data,
                currentPos,
                new ParticleTreeNodeStylingProfile(role, leftPos, middlePos, rightPos).applyStyle(node.data.toString(), currentPos)
        );

        assignPositions(node.left, origin, depth + 1, xOffset - step, maxDepth, out);
        assignPositions(node.middle, origin, depth + 1, xOffset, maxDepth, out);
        assignPositions(node.right, origin, depth + 1, xOffset + step, maxDepth, out);
    }

    private Pos buildPosition(Pos origin, int depth, double xOffset) {
        return new Pos(
                origin.x() + xOffset,
                origin.y() + rootYOffset - (depth * levelDrop),
                origin.z() + zOffset + (depth * 0.45)
        );
    }

    private static class Node<T> {
        final T data;
        final int originalIndex;
        Node<T> left;
        Node<T> middle;
        Node<T> right;

        Node(T data, int originalIndex) {
            this.data = data;
            this.originalIndex = originalIndex;
        }
    }
}

