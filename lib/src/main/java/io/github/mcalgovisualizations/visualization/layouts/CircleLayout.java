package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.Displays.AbstractParticleDisplay;
import io.github.mcalgovisualizations.visualization.renderer.Displays.BlockDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.List;

public record CircleLayout(double radius, double yOffset) implements ILayout {

    public CircleLayout() {
        this(2.0, 2.0);
    }
    public CircleLayout (double radius) {
        this(radius, 2.0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin, Instance instance) {
        var size = model.size();
        var out = new LayoutResult[size];

        double y = origin.y() + yOffset;

        if (size == 0) return new LayoutResult[0];

        if (size == 1) {
            out[0] = new LayoutResult<>(model.getFirst(), origin, new StylingProfile());
            return out;
        }

        if (isFlowMatrix(model)) {
            int nodeCount = (int) Math.sqrt(size);
            Pos[] nodePositions = buildCircleNodes(origin, nodeCount, y);

            for (int from = 0; from < nodeCount; from++) {
                for (int to = 0; to < nodeCount; to++) {
                    int idx = (from * nodeCount) + to;
                    Data<T> data = model.get(idx);
                    int capacity = readCapacity(data.value());

                    Pos a = nodePositions[from];
                    Pos b = nodePositions[to];
                    Pos midpoint = new Pos(
                            (a.x() + b.x()) * 0.5,
                            y + 0.8,
                            (a.z() + b.z()) * 0.5
                    );

                    out[idx] = new LayoutResult<>(
                            data,
                            midpoint,
                            new FlowEdgeStylingProfile(capacity, a, b)
                    );
                }
            }

            return out;
        }

        for (int i = 0; i < size; i++) {
            double angle = (2.0 * Math.PI * i) / size;
            double x = origin.x() + (Math.cos(angle) * radius);
            double z = origin.z() + (Math.sin(angle) * radius);
            final var pos = new Pos(x, y, z);
            out[i] = new LayoutResult<>(model.get(i), pos, new StylingProfile());
        }

        return out;
    }

    private Pos[] buildCircleNodes(Pos origin, int nodeCount, double y) {
        Pos[] nodes = new Pos[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            double angle = (2.0 * Math.PI * i) / nodeCount;
            double x = origin.x() + (Math.cos(angle) * radius);
            double z = origin.z() + (Math.sin(angle) * radius);
            nodes[i] = new Pos(x, y, z);
        }
        return nodes;
    }

    private static <T extends Comparable<T>> boolean isFlowMatrix(List<Data<T>> model) {
        int size = model.size();
        int n = (int) Math.sqrt(size);
        if (n * n != size || n < 2) {
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
            Block block = capacity > 0 ? Block.LIGHT_BLUE_CONCRETE : Block.GRAY_CONCRETE;
            BlockDisplay base = new BlockDisplay(pos, block, value);
            return new FlowEdgeParticles(base, capacity, from, to);
        }
    }

    private static final class FlowEdgeParticles extends AbstractParticleDisplay {
        private final int capacity;
        private final Pos from;
        private final Pos to;

        private FlowEdgeParticles(BlockDisplay base, int capacity, Pos from, Pos to) {
            super(base);
            this.capacity = capacity;
            this.from = from;
            this.to = to;
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
