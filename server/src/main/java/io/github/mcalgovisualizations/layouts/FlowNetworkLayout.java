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

                Pos a = nodePositions[from];
                Pos b = nodePositions[to];
                Pos midpoint = new Pos(
                        (a.x() + b.x()) * 0.5,
                        y + 0.8,
                        (a.z() + b.z()) * 0.5
                );

                out[idx] = new LayoutResult<>(data, midpoint, new FlowEdgeStylingProfile(capacity, a, b));
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

    private static final class FlowEdgeStylingProfile implements IStylingProfile {
        private final int capacity;
        private final Pos from;
        private final Pos to;

        private FlowEdgeStylingProfile(int capacity, Pos from, Pos to) {
            this.capacity = capacity;
            this.from = from;
            this.to = to;
        }

        @Override
        public IDisplayValue applyStyle(String value, Pos pos) {
            boolean hasCapacity = capacity > 0;
            Block block = hasCapacity ? Block.LIGHT_BLUE_CONCRETE : Block.GRAY_CONCRETE;
            String label = hasCapacity ? Integer.toString(capacity) : "";
            BlockDisplay base = new BlockDisplay(pos, block, label, hasCapacity);
            return new FlowEdgeParticles(base, capacity, from, to);
        }
    }

    private static final class FlowEdgeParticles extends AbstractParticleDisplay {
        private final int capacity;
        private final Pos from;
        private final Pos to;
        private boolean selectedPath;

        private FlowEdgeParticles(BlockDisplay base, int capacity, Pos from, Pos to) {
            super(base);
            this.capacity = capacity;
            this.from = from;
            this.to = to;
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
            if (capacity <= 0 || from.samePoint(to)) {
                return List.of();
            }
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
