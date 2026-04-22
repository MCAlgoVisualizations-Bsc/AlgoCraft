package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.Displays.AbstractParticleDisplay;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.visualization.IStylingProfile;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;

import java.util.ArrayList;
import java.util.List;

public record GraphNetworkLayout<T> (double xSpacing, double yOffset) implements ILayout<List<T>> {
    private static final int NODE_COUNT = FlowNetworkRules.NODE_COUNT;
    private static final int MATRIX_SIZE = FlowNetworkRules.MATRIX_SIZE;
    private static final double Z_SPREAD = 7.0;
    private static final int NODE_B = 1;
    private static final int NODE_C = 2;
    private static final int NODE_D = 3;
    private static final int NODE_E = 4;

    public GraphNetworkLayout() {
        this(8.0, 5.0);
    }

    @Override
    public LayoutResult[] compute(List<T> model, Pos origin, Instance instance) {
        int size = model.size();
        var out = new LayoutResult[size];
        if (size == 0) {
            return out;
        }

        if (!isFixedFlowMatrix(model)) {
            throw new IllegalArgumentException("FlowNetworkLayout requires exactly 36 integer values (6x6 matrix)");
        }

        double y = origin.y() + yOffset;
        Pos[] nodePositions = buildFixedNodes(origin, y);

        for (int from = 0; from < NODE_COUNT; from++) {
            for (int to = 0; to < NODE_COUNT; to++) {
                int idx = (from * NODE_COUNT) + to;
                var data = model.get(idx);
                int capacity = FlowNetworkRules.effectiveCapacity(from, to, readCapacity(data));

                if (from == to) {
                    out[idx] = new LayoutResult(data, nodePositions[from], new NodeStylingProfile(nodeName(from), from == NODE_COUNT - 1));
                    continue;
                }

                if (capacity <= 0) {
                    out[idx] = new LayoutResult(
                            data,
                            edgeLabelPos(nodePositions[from], nodePositions[to], y, 0, false),
                            new HiddenStylingProfile()
                    );
                    continue;
                }

                Pos fromPos = nodePositions[from];
                Pos toPos = nodePositions[to];
                int reciprocalSign = reciprocalOffsetSign(from, to);
                int offsetSign = reciprocalSign != 0
                        ? reciprocalSign
                        : crossingOffsetSign(from, to, model);
                boolean reciprocal = reciprocalSign != 0;
                Pos labelPos = edgeLabelPos(fromPos, toPos, y, offsetSign, reciprocal);

                out[idx] = new LayoutResult(
                        data,
                        labelPos,
                        new FlowEdgeStylingProfile(capacity, fromPos, toPos, nodeName(from), nodeName(to), offsetSign)
                );
            }
        }

        return out;
    }

    private Pos[] buildFixedNodes(Pos origin, double y) {
        double x0 = origin.x();
        double x1 = x0 + xSpacing;
        double x2 = x0 + (xSpacing * 2.0);
        double x3 = x0 + (xSpacing * 3.0);
        double z = origin.z();

        return new Pos[] {
                new Pos(x0, y, z),
                new Pos(x1, y, z - Z_SPREAD),
                new Pos(x1, y, z + Z_SPREAD),
                new Pos(x2, y, z - Z_SPREAD),
                new Pos(x2, y, z + Z_SPREAD),
                new Pos(x3, y, z)
        };
    }

    private static Pos edgeLabelPos(Pos from, Pos to, double baseY, int offsetSign, boolean reciprocal) {
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        double length = Math.sqrt((dx * dx) + (dz * dz));

        double t = reciprocal ? 0.38 : 0.5; // Bidirectional labels sit closer to each source node.

        double nx = 0.0;
        double nz = 0.0;
        if (length > 0.0001 && offsetSign != 0) {
            nx = (-dz / length) * 0.9 * offsetSign;
            nz = (dx / length) * 0.9 * offsetSign;
        }

        return new Pos(
                from.x() + (dx * t) + nx,
                baseY + 1.0,
                from.z() + (dz * t) + nz
        );
    }

    private static char nodeName(int idx) {
        return (char) ('A' + idx);
    }

    private static boolean isAllowedDirectedEdge(int from, int to) {
        return FlowNetworkRules.isAllowedDirectedEdge(from, to);
    }

    private static int reciprocalOffsetSign(int from, int to) {
        if (!isAllowedDirectedEdge(from, to) || !isAllowedDirectedEdge(to, from)) {
            return 0;
        }
        return from < to ? 1 : -1;
    }

    private int crossingOffsetSign(int from, int to, List<T> model) {
        boolean isBtoE = from == NODE_B && to == NODE_E;
        boolean isCtoD = from == NODE_C && to == NODE_D;
        if (!isBtoE && !isCtoD) {
            return 0;
        }

        int bToECapacity = readCapacity(model.get((NODE_B * NODE_COUNT) + NODE_E));
        int cToDCapacity = readCapacity(model.get((NODE_C * NODE_COUNT) + NODE_D));
        if (bToECapacity <= 0 || cToDCapacity <= 0) {
            return 0;
        }

        return isBtoE ? 1 : -1;
    }

    private boolean isFixedFlowMatrix(List<T> model) {
        if (model.size() != MATRIX_SIZE) {
            return false;
        }
        for (T cell : model) {
            if (!(cell instanceof Integer)) {
                return false;
            }
        }
        return true;
    }

    private static int readCapacity(Object raw) {
        return raw instanceof Integer number ? Math.max(0, number) : 0;
    }

    private static final class NodeStylingProfile implements IStylingProfile {
        private final char name;
        private final boolean sinkNode;

        private NodeStylingProfile(char name, boolean sinkNode) {
            this.name = name;
            this.sinkNode = sinkNode;
        }

        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            return new NodeDisplay(pos, name, sinkNode);
        }
    }

    private static final class NodeDisplay extends BlockDisplay {
        private final char name;
        private final boolean sinkNode;

        private NodeDisplay(Pos pos, char name, boolean sinkNode) {
            super(pos, Block.BLACK_CONCRETE, Character.toString(name), true);
            this.name = name;
            this.sinkNode = sinkNode;
        }

        @Override
        public void setValue(int value) {
            int clamped = Math.max(0, value);
            if (sinkNode) {
                setText(name + " " + clamped);
            } else {
                setText(Character.toString(name));
            }
            setBlock(colorForLoad(clamped));
        }

        private static Block colorForLoad(int load) {
            if (load >= 12) return Block.RED_CONCRETE;
            if (load >= 8) return Block.ORANGE_CONCRETE;
            if (load >= 4) return Block.YELLOW_CONCRETE;
            if (load >= 1) return Block.LIGHT_BLUE_CONCRETE;
            return Block.BLACK_CONCRETE;
        }
    }

    private static final class HiddenStylingProfile implements IStylingProfile {
        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            return new BlockDisplay(pos, Block.AIR, "", false);
        }
    }

    private static final class FlowEdgeStylingProfile implements IStylingProfile {
        private final int capacity;
        private final Pos from;
        private final Pos to;
        private final char fromName;
        private final char toName;
        private final int offsetSign;

        private FlowEdgeStylingProfile(int capacity, Pos from, Pos to, char fromName, char toName, int offsetSign) {
            this.capacity = capacity;
            this.from = from;
            this.to = to;
            this.fromName = fromName;
            this.toName = toName;
            this.offsetSign = offsetSign;
        }

        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            String label = fromName + "->" + toName + " 0/" + capacity;
            BlockDisplay base = new BlockDisplay(pos, Block.AIR, label, true);
            return new FlowEdgeParticles(base, capacity, from, to, fromName, toName, offsetSign);
        }
    }

    private static final class FlowEdgeParticles extends AbstractParticleDisplay {
        private final int capacity;
        private final Pos from;
        private final Pos to;
        private final char fromName;
        private final char toName;
        private final int offsetSign;
        private boolean selectedPath;
        private int currentFlow;

        private FlowEdgeParticles(BlockDisplay base, int capacity, Pos from, Pos to, char fromName, char toName, int offsetSign) {
            super(base);
            this.capacity = capacity;
            this.from = particleAnchor(from);
            this.to = particleAnchor(to);
            this.fromName = fromName;
            this.toName = toName;
            this.offsetSign = offsetSign;
        }

        private static Pos particleAnchor(Pos nodePos) {
            return nodePos.add(0.5, 0.5, 0.5);
        }

        @Override
        public void setValue(int value) {
            this.currentFlow = Math.max(0, value);
            ((BlockDisplay) base).setText(fromName + "->" + toName + " " + currentFlow + "/" + capacity);
        }

        @Override
        public void setGlowing(boolean highlighted) {
            super.setGlowing(highlighted);
            this.selectedPath = highlighted;
        }

        @Override
        protected Particle particle() {
            return selectedPath ? Particle.FLAME : Particle.CRIT;
        }

        @Override
        protected Pos particleSource() {
            return parallelSources().isEmpty() ? from : parallelSources().getFirst();
        }

        @Override
        protected List<Pos> particleSources() {
            return parallelSources();
        }

        @Override
        protected List<Pos> targets() {
            return parallelTargets();
        }

        @Override
        protected int particlesPerStep() {
            int base = 1 + (capacity / 3);
            int flowBoost = currentFlow / 3;
            return Math.min(18, Math.max(1, base + flowBoost));
        }

        private List<Pos> parallelSources() {
            return buildParallelPoints(from, true);
        }

        private List<Pos> parallelTargets() {
            return buildParallelPoints(to, false);
        }

        private List<Pos> buildParallelPoints(Pos basePoint, boolean sourceSide) {
            int lanes = Math.max(1, Math.min(5, 1 + (capacity / 6)));
            if (lanes == 1) {
                return List.of(offsetPoint(basePoint));
            }

            List<Pos> points = new ArrayList<>(lanes);
            double step = 0.16;
            double center = (lanes - 1) / 2.0;
            for (int i = 0; i < lanes; i++) {
                double lateral = (i - center) * step;
                Pos shifted = applyLateralOffset(offsetPoint(basePoint), lateral);
                // Slightly nudge away from node to make arrow direction clearer at endpoints.
                points.add(nudgeAlongEdge(shifted, sourceSide ? 0.18 : -0.18));
            }
            return points;
        }

        private Pos offsetPoint(Pos point) {
            return point;
        }

        private Pos applyLateralOffset(Pos point, double amount) {
            double dx = to.x() - from.x();
            double dz = to.z() - from.z();
            double length = Math.sqrt((dx * dx) + (dz * dz));
            if (length <= 0.0001) {
                return point;
            }
            double nx = -dz / length;
            double nz = dx / length;
            return new Pos(point.x() + (nx * amount), point.y(), point.z() + (nz * amount));
        }

        private Pos nudgeAlongEdge(Pos point, double amount) {
            double dx = to.x() - from.x();
            double dy = to.y() - from.y();
            double dz = to.z() - from.z();
            double length = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
            if (length <= 0.0001) return point;
            return new Pos(
                    point.x() + ((dx / length) * amount),
                    point.y() + ((dy / length) * amount),
                    point.z() + ((dz / length) * amount)
            );
        }
    }
}
