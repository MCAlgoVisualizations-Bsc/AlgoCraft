package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Places elements randomly within a bounded 2D grid (matrix) centered on origin.
 * Produces positions only (no entities).
 */
public record MatrixLayout (
    double  yOffset,
    int     cols,
    int     rows,
    double  spacing,
    long    seed
) implements ILayout {

    // setting the default configs here
    public MatrixLayout() {
        this(2.0,10, 10, 24);
    }
    public MatrixLayout(double yOffset, int cols, int rows) {
        this(yOffset, cols, rows, 24);
    }
    public MatrixLayout(double yOffset, int cols, int rows, int seed) {
        this(yOffset, cols, rows, 2.0, seed);
    }

    /**
     * Deterministic layout for {@code size} nodes using this instance's RNG.
     * Throws if size exceeds capacity.
     */
    @Override
    public <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin) {
        return random(model, origin);
    }

    /**
     * Randomized (but deterministic per seed) grid positions.
     */
    public <T extends Comparable<T>> LayoutResult<T>[] random(List<Data<T>> model, Pos origin) {
        final var size = model.size();
        final var random = new Random(seed);
        int capacity = cols * rows;
        if (size > capacity) {
            throw new IllegalArgumentException(
                    "Grid too small: size=" + size + " capacity=" + capacity +
                            " (cols=" + cols + ", rows=" + rows + ")"
            );
        }

        var out = new LayoutResult[size];

        double y = origin.y() + yOffset;

        double gridWidth = (cols - 1) * spacing;
        double gridHeight = (rows - 1) * spacing;

        double startX = origin.x() - gridWidth / 2.0;
        double startZ = origin.z() - gridHeight / 2.0;

        // All cells
        List<Cell> cells = new ArrayList<>(capacity);
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells.add(new Cell(c, r));
            }
        }

        // Shuffle deterministically
        Collections.shuffle(cells, random);

        // First 'size' cells become node positions (node id == index)
        for (int id = 0; id < size; id++) {
            Cell cell = cells.get(id);
            final double x = startX + cell.col * spacing;
            final double z = startZ + cell.row * spacing;
            out[id] = new LayoutResult<>(new Data<>(id), new Pos(x, y, z));
        }

        return out;
    }

    private record Cell(int col, int row) { }
}
