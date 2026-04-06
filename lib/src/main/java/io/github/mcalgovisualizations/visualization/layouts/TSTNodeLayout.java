package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.Displays.BlockDisplay;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

import java.util.List;
import java.util.ArrayList;

/**
 * Places values by simulating a ternary search tree insertion to determine logical
 * parent/left/middle/right relationships, then assigns coordinates based on tree depth.
 */
public record TSTNodeLayout(
        double rootYOffset,
        double levelDrop,
        double horizontalSpacing,
        double zOffset
) implements ILayout {

    public TSTNodeLayout() {
        this(4.0, 2.0, 0.5, 0.0);
    }

    public TSTNodeLayout {
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

        Node<T> root = new Node<>(model.getFirst(), 0);
        for (int i = 1; i < size; i++) {
            insert(root, model.get(i), i);
        }

        int maxDepth = getMaxDepth(root);
        assignPositions(root, origin, 0, 0.0, maxDepth, out);
        return out;
    }

    private <T extends Comparable<T>> void insert(Node<T> root, Data<T> data, int index) {
        Node<T> current = root;
        T candidate = data.value();

        while (true) {
            T currentValue = current.data.value();
            int cmp = candidate.compareTo(currentValue);
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
                    current.middle = new Node<>(data, index);
                    break;
                }
                current = current.middle;
            }
        }
    }

    private <T extends Comparable<T>> int getMaxDepth(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(node.left == null ? 0 : getMaxDepth(node.left),
                Math.max(node.middle == null ? 0 : getMaxDepth(node.middle),
                        node.right == null ? 0 : getMaxDepth(node.right)));
    }

    private <T extends Comparable<T>> void assignPositions(Node<T> node, Pos origin, int depth, double xOffset, int maxDepth, LayoutResult<T>[] out) {
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

        out[node.originalIndex] = new LayoutResult<>(node.data, currentPos, new TstNodeStylingProfile(role, leftPos, middlePos, rightPos));

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

    private static final class TstNodeStylingProfile implements IStylingProfile {
        private final BstNodeStylingProfile.NodeRole role;
        private final Pos leftPos;
        private final Pos middlePos;
        private final Pos rightPos;

        private TstNodeStylingProfile(BstNodeStylingProfile.NodeRole role, Pos leftPos, Pos middlePos, Pos rightPos) {
            this.role = role;
            this.leftPos = leftPos;
            this.middlePos = middlePos;
            this.rightPos = rightPos;
        }

        @Override
        public io.github.mcalgovisualizations.visualization.renderer.IDisplayValue applyStyle(String value, Pos pos) {
            Block nodeBlock = switch (role) {
                case ROOT -> net.minestom.server.instance.block.Block.OAK_LOG;
                case LEAF -> net.minestom.server.instance.block.Block.OAK_LEAVES;
                case INTERNAL -> net.minestom.server.instance.block.Block.OAK_PLANKS;
            };
            return new TstNodeWithConnectors(new BlockDisplay(pos, nodeBlock, value), pos, leftPos, middlePos, rightPos);
        }
    }

    private static final class TstNodeWithConnectors implements io.github.mcalgovisualizations.visualization.renderer.IDisplayValue {
        private final BlockDisplay base;
        private final Pos nodePos;
        private final Pos leftPos;
        private final Pos middlePos;
        private final Pos rightPos;
        private final List<BlockDisplay> connectors = new ArrayList<>();

        private TstNodeWithConnectors(BlockDisplay base, Pos nodePos, Pos leftPos, Pos middlePos, Pos rightPos) {
            this.base = base;
            this.nodePos = nodePos;
            this.leftPos = leftPos;
            this.middlePos = middlePos;
            this.rightPos = rightPos;
        }

        @Override
        public Pos getPos() {
            return base.getPos();
        }

        @Override
        public void setValue(int value) {
            base.setValue(value);
        }

        @Override
        public void setInstance(Instance instance) {
            base.setInstance(instance);
            createConnectors(instance, nodePos, leftPos);
            createConnectors(instance, nodePos, middlePos);
            createConnectors(instance, nodePos, rightPos);
        }

        @Override
        public void addViewer(Player player) {
            base.addViewer(player);
            for (var connector : connectors) {
                connector.addViewer(player);
            }
        }

        @Override
        public void remove() {
            base.remove();
            for (var connector : connectors) {
                connector.remove();
            }
            connectors.clear();
        }

        @Override
        public void teleport(Pos pos) {
            base.teleport(pos);
        }

        @Override
        public void setGlowing(boolean highlighted) {
            base.setGlowing(highlighted);
        }

        @Override
        public boolean isSpawned() {
            return base.isSpawned();
        }

        private void createConnectors(Instance instance, Pos from, Pos to) {
            if (to == null || instance == null) {
                return;
            }

            double dx = to.x() - from.x();
            double dy = to.y() - from.y();
            double dz = to.z() - from.z();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int steps = Math.max(3, (int) Math.ceil(distance * 2.0));

            for (int i = 1; i < steps; i++) {
                double t = (double) i / steps;
                Pos point = new Pos(
                        from.x() + (dx * t),
                        from.y() + (dy * t),
                        from.z() + (dz * t)
                );
                BlockDisplay connector = new BlockDisplay(instance, point, Block.GRAY_CONCRETE, "");
                connectors.add(connector);
            }
        }
    }

    private static class Node<T extends Comparable<T>> {
        final Data<T> data;
        final int originalIndex;
        Node<T> left;
        Node<T> middle;
        Node<T> right;

        Node(Data<T> data, int originalIndex) {
            this.data = data;
            this.originalIndex = originalIndex;
        }
    }
}

