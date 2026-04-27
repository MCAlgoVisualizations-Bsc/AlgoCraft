package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashSet;
import java.util.Set;

public class LayoutPath implements ILayout<Node> {

    private static final int SCALE = 2;
    private static final int SPACING = 10 * SCALE;
    private final int gridCols;

    public LayoutPath(int gridCols) {
        this.gridCols = gridCols;
    }

    @Override
    public LayoutResult[] compute(Node model, Pos origin, Instance instance) {
        if (model == null) return new LayoutResult[0];

        int maxId = findMaxId(model, new HashSet<>());
        LayoutResult[] results = new LayoutResult[maxId + 1];

        double floorY = Math.floor(origin.y()) - 1;
        Pos floorOrigin = new Pos(origin.x(), floorY, origin.z());

        // Clear previous paths/blocks in the expected grid area
        // Assuming the grid is roughly rows x gridCols. NodeUtils uses 4 rows and 6 cols.
        // We can dynamically find the max row as well.
        int maxRow = (maxId / gridCols) + 1;
        clearArea(floorOrigin, instance, gridCols, maxRow);

        renderGraph(model, floorOrigin, instance, results, new HashSet<>());

        return results;
    }

    private void clearArea(Pos origin, Instance instance, int cols, int rows) {
        // Clear a slightly larger area to be safe
        int margin = 5;
        for (int r = -margin; r < rows * SPACING + margin; r++) {
            for (int c = -margin; c < cols * SPACING + margin; c++) {
                instance.setBlock(origin.add(c, 0, r), Block.GRASS_BLOCK);
            }
        }
    }

    private int findMaxId(Node node, Set<Integer> visited) {
        if (node == null || visited.contains(node.id())) return -1;
        visited.add(node.id());
        int max = node.id();
        for (Node neighbor : node.neighbors()) {
            max = Math.max(max, findMaxId(neighbor, visited));
        }
        return max;
    }

    private void renderGraph(Node node, Pos origin, Instance instance,
                             LayoutResult[] results, Set<Integer> visited) {

        if (node == null || visited.contains(node.id())) return;
        visited.add(node.id());

        int xIdx = node.id() % gridCols;
        int zIdx = node.id() / gridCols;

        Pos currentPos = origin.add(xIdx * SPACING, 0, zIdx * SPACING);
        instance.setBlock(currentPos, Block.WHITE_WOOL);

        results[node.id()] = new LayoutResult(node.value(), currentPos, new NodeDisplay(String.valueOf(node.value()), currentPos));

        for (Node neighbor : node.neighbors()) {
            if (!visited.contains(neighbor.id())) {
                int nextX = neighbor.id() % gridCols;
                int nextZ = neighbor.id() / gridCols;

                Pos neighborPos = origin.add(nextX * SPACING, 0, nextZ * SPACING);
                drawConnection(currentPos, neighborPos, instance, Block.DIRT_PATH);

                renderGraph(neighbor, origin, instance, results, visited);
            } else {
                LayoutResult neighborResult = results[neighbor.id()];
                if (neighborResult != null) {
                    drawConnection(currentPos, neighborResult.pos(), instance, Block.DIRT_PATH);
                }
            }
        }
    }

    private void drawConnection(Pos start, Pos end, Instance instance, Block block) {
        double dist = start.distance(end);
        if (dist < 1) return;

        int steps = (int) Math.ceil(dist);
        double dx = end.x() - start.x();
        double dz = end.z() - start.z();
        double length = Math.sqrt(dx * dx + dz * dz);

        double nx = length > 0 ? -dz / length : 0;
        double nz = length > 0 ? dx / length : 0;

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            double centerX = start.x() + dx * ratio;
            double centerZ = start.z() + dz * ratio;

            for (int offset = -1; offset <= 1; offset++) {
                double finalX = centerX + (nx * offset);
                double finalZ = centerZ + (nz * offset);
                instance.setBlock(new Pos(Math.round(finalX), start.y(), Math.round(finalZ)), block);
            }
        }
    }
}
