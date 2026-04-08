package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.Displays.AbstractParticleDisplay;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.visualization.ILayout;
import io.github.mcalgovisualizations.visualization.IStylingProfile;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;

import java.util.List;

public record FlowNetworkLayout(double xSpacing, double yOffset) implements ILayout {
    private static final int NODE_COUNT = 6;
    private static final int MATRIX_SIZE = NODE_COUNT * NODE_COUNT;
    private static final double Z_SPREAD = 7.0;
    // Directed adjacency constraints for A..F requested by the flow-graph spec.
    private static final boolean[][] ALLOWED = {
            {false, true,  true,  false, false, false}, // A -> B,C
            {true,  false, true,  true,  true,  false}, // B -> A,C,D,E
            {true,  true,  false, true,  true,  false}, // C -> A,B,D,E
            {false, true,  true,  false, true,  true }, // D -> B,C,E,F
            {false, true,  true,  true,  false, true }, // E -> B,C,D,F
            {false, false, false, true,  true,  false}  // F -> D,E
    };

    public FlowNetworkLayout() {
        this(8.0, 5.0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin, Instance instance) {
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
                Data<T> data = model.get(idx);
                int capacity = readCapacity(data.value());

                if (from == to) {
                    out[idx] = new LayoutResult<>(data, nodePositions[from], new NodeStylingProfile(nodeName(from)));
                    continue;
                }

                if (!isAllowedDirectedEdge(from, to) || capacity <= 0) {
                    out[idx] = new LayoutResult<>(data, edgeMidpoint(nodePositions[from], nodePositions[to], y, 0), new HiddenStylingProfile());
                    continue;
                }

                Pos fromPos = nodePositions[from];
                Pos toPos = nodePositions[to];
                Pos midpoint = edgeMidpoint(fromPos, toPos, y, shouldOffsetLabel(from, to));
                out[idx] = new LayoutResult<>(
                        data,
                        midpoint,
                        new FlowEdgeStylingProfile(capacity, fromPos, toPos, nodeName(from), nodeName(to))
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

    private static Pos edgeMidpoint(Pos from, Pos to, double baseY, int offsetSign) {
        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        double length = Math.sqrt((dx * dx) + (dz * dz));
        double nx = 0.0;
        double nz = 0.0;
        if (length > 0.0001 && offsetSign != 0) {
            // Perpendicular offset keeps opposite-direction labels from overlapping.
            nx = (-dz / length) * 0.9 * offsetSign;
            nz = (dx / length) * 0.9 * offsetSign;
        }
        return new Pos(
                ((from.x() + to.x()) * 0.5) + nx,
                baseY + 1.0,
                ((from.z() + to.z()) * 0.5) + nz
        );
    }

    private static boolean isAllowedDirectedEdge(int from, int to) {
        return from >= 0 && to >= 0 && from < NODE_COUNT && to < NODE_COUNT && ALLOWED[from][to];
    }

    private static int shouldOffsetLabel(int from, int to) {
        if (!isAllowedDirectedEdge(from, to) || !isAllowedDirectedEdge(to, from)) {
            return 0;
        }
        return from < to ? 1 : -1;
    }

    private static char nodeName(int idx) {
        return (char) ('A' + idx);
    }

    private static <T extends Comparable<T>> boolean isFixedFlowMatrix(List<Data<T>> model) {
        if (model.size() != MATRIX_SIZE) {
            return false;
        }
        for (Data<T> cell : model) {
            if (!(cell.value() instanceof Integer)) {
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

        private NodeStylingProfile(char name) {
            this.name = name;
        }

        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            return new BlockDisplay(pos, Block.BLACK_CONCRETE, Character.toString(name), true);
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

        private FlowEdgeStylingProfile(int capacity, Pos from, Pos to, char fromName, char toName) {
            this.capacity = capacity;
            this.from = from;
            this.to = to;
            this.fromName = fromName;
            this.toName = toName;
        }

        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            String label = fromName + "->" + toName + " 0/" + capacity;
            BlockDisplay base = new BlockDisplay(pos, Block.AIR, label, true);
            return new FlowEdgeParticles(base, capacity, from, to, fromName, toName);
        }
    }

    private static final class FlowEdgeParticles extends AbstractParticleDisplay {
        private final int capacity;
        private final Pos from;
        private final Pos to;
        private final char fromName;
        private final char toName;
        private boolean selectedPath;
        private int currentFlow;

        private FlowEdgeParticles(BlockDisplay base, int capacity, Pos from, Pos to, char fromName, char toName) {
            super(base);
            this.capacity = capacity;
            this.from = from;
            this.to = to;
            this.fromName = fromName;
            this.toName = toName;
        }

        @Override
        public void setValue(int value) {
            this.currentFlow = Math.max(0, value);
            base.setText(fromName + "->" + toName + " " + currentFlow + "/" + capacity);
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
        protected List<Pos> targets() {
            return List.of(to);
        }

        @Override
        protected Pos particleSource() {
            return from;
        }

        @Override
        protected int particlesPerStep() {
            return Math.min(8, 1 + (capacity / 2));
        }
    }
}


