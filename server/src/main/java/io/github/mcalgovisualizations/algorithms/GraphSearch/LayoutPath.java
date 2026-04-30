package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class LayoutPath implements ILayout<Node> {

    private static final int SCALE = 2;
    private static final int SPACING = 10 * SCALE;
    private final int gridCols;

    private final Block pathBlock = Block.DIRT_PATH;

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
                // Also clear above to remove potential houses
                for (int y = 1; y < 6; y++) {
                    instance.setBlock(origin.add(c, y, r), Block.AIR);
                }
            }
        }
    }

    private int findMaxId(Node node, Set<Integer> visited) {
        if (node == null || visited.contains(node.getID())) return -1;
        visited.add(node.getID());
        int max = node.getID();
        for (Node neighbor : node.getNeighbors()) {
            max = Math.max(max, findMaxId(neighbor, visited));
        }
        return max;
    }

    private void renderGraph(Node node, Pos origin, Instance instance,
                             LayoutResult[] results, Set<Integer> visited) {

        if (node == null || visited.contains(node.getID())) return;
        visited.add(node.getID());

        int xIdx = node.getID() % gridCols;
        int zIdx = node.getID() / gridCols;

        Pos currentPos = origin.add(xIdx * SPACING, 0, zIdx * SPACING);

        // --- NEW LOGIC: Block Selection based on Status ---
        Block nodeBlock;
        switch (node.getStatus()) {
            case Start -> {
                    nodeBlock = Block.LIME_WOOL;
                    instance.setBlock(currentPos.add(0,10,0),Block.LIME_WOOL);
            }  // Green for Start
            case End   -> {
                nodeBlock = Block.RED_WOOL;   // Red for End
                buildSmallHouse(currentPos, instance);
            }
            default    -> nodeBlock = Block.WHITE_WOOL; // Default
        }

        instance.setBlock(currentPos, nodeBlock);
        // --------------------------------------------------

        results[node.getID()] = new LayoutResult(
                node.getValue(),
                currentPos,
                new NodeDisplay(String.valueOf(node.getValue()), currentPos)
        );

        for (Node neighbor : node.getNeighbors()) {
            // ... (rest of your connection logic remains the same) ...
            if (!visited.contains(neighbor.getID())) {
                int nextX = neighbor.getID() % gridCols;
                int nextZ = neighbor.getID() / gridCols;

                Pos neighborPos = origin.add(nextX * SPACING, 0, zIdx * SPACING); // Fixed potential bug from original code where nextZ was used but xIdx was being added to gridCols
                // Wait, original code was: Pos neighborPos = origin.add(nextX * SPACING, 0, nextZ * SPACING);
                // Restoring original connection logic to avoid regression
                Pos originalNeighborPos = origin.add(nextX * SPACING, 0, nextZ * SPACING);
                drawConnection(currentPos, originalNeighborPos, instance, pathBlock);

                renderGraph(neighbor, origin, instance, results, visited);
            } else {
                LayoutResult neighborResult = results[neighbor.getID()];
                if (neighborResult != null) {
                    drawConnection(currentPos, neighborResult.pos(), instance, pathBlock);
                }
            }
        }
    }

    private void buildSmallHouse(Pos pos, Instance instance) {
        // pos = pos.add(0, 1, 0);
        final int size = 2;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                // Floor
                instance.setBlock(pos.add(x, 0, z), Block.COBBLESTONE);
                // Wall
                if (x == -size || x == size || z == -size || z == size) {
                    instance.setBlock(pos.add(x, 1, z), Block.OAK_PLANKS);
                    instance.setBlock(pos.add(x, 2, z), Block.OAK_PLANKS);
                    instance.setBlock(pos.add(x, 3, z), Block.OAK_PLANKS);
                }
                // Door
                if (x == 0 || z == 0) {
                    instance.setBlock(pos.add(x, 1, z), Block.AIR);
                    instance.setBlock(pos.add(x, 2, z), Block.AIR);
                }
                // Corners
                if (Math.abs(x) == size && Math.abs(z) == size) {
                    instance.setBlock(pos.add(x, 1, z), Block.OAK_LOG);
                    instance.setBlock(pos.add(x, 2, z), Block.OAK_LOG);
                    instance.setBlock(pos.add(x, 3, z), Block.OAK_LOG);
                }

                // Roof
                instance.setBlock(pos.add(x, 4, z), Block.OAK_LOG);
            }
        }
        /*
        instance.setBlock(pos.add(-size - 1, 0, 0), Block.COBBLESTONE_STAIRS.withProperty("facing", "east"));
        instance.setBlock(pos.add(size + 1, 0, 0), Block.COBBLESTONE_STAIRS.withProperty("facing", "west"));
        instance.setBlock(pos.add(0, 0, -size - 1), Block.COBBLESTONE_STAIRS.withProperty("facing", "south"));
        instance.setBlock(pos.add(0, 0, size + 1), Block.COBBLESTONE_STAIRS.withProperty("facing", "north"));
        */
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
