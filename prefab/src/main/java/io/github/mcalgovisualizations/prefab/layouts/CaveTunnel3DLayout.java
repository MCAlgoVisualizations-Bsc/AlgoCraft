package io.github.mcalgovisualizations.prefab.layouts;

import io.github.mcalgovisualizations.prefab.Displays.MobDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
@Deprecated
public record CaveTunnel3DLayout(
        int columns,
        int layers,
        int depth,
        double cellSize,
        double yOffset,
        double zOffset
) implements ILayout<List<Integer>> {
    public CaveTunnel3DLayout(int columns, int layers, int depth) {
        this(columns, layers, depth, 4.0, 0.0, 0.0);
    }

    public CaveTunnel3DLayout {
        if (columns <= 0 || layers <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Cave dimensions must be > 0");
        }
        if (cellSize <= 0) {
            throw new IllegalArgumentException("cellSize must be > 0");
        }
    }

    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        if (model.size() != columns * layers * depth) {
            throw new IllegalArgumentException("Cave model size mismatch");
        }

        LayoutResult[] out = new LayoutResult[model.size()];
        for (int slot = 0; slot < model.size(); slot++) {
            int x = slot % columns;
            int y = (slot / columns) % layers;
            int z = slot / (columns * layers);

            Pos pos = new Pos(
                    origin.x() + (x * cellSize),
                    origin.y() + yOffset + (y * cellSize),
                    origin.z() + zOffset + (z * cellSize)
            );
            out[slot] = new LayoutResult(model.get(slot), pos, new MobDisplay(pos, model.get(slot).toString()));
        }

        return out;
    }
}

