package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class LayoutPath<V extends Comparable<V>> implements ILayout<Node<V>> {

    private static final int SCALE = 2;
    private static final int SPACING = 10 * SCALE;

    // Helper class to track occupied grid coordinates
    private record GridPos(int x, int z) {}

    public LayoutPath() {}

    @Override
    public LayoutResult[] compute(Node<V> model, Pos origin, Instance instance) {
        if (model == null) return new LayoutResult[0];

        int maxId = findMaxId(model, new HashSet<>());
        LayoutResult[] results = new LayoutResult[maxId + 1];

        double floorY = Math.floor(origin.y()) - 1;
        Pos floorOrigin = new Pos(origin.x(), floorY, origin.z());

        // Track occupied grid cells to prevent stacking
        Set<GridPos> occupiedCells = new HashSet<>();

        renderGraph(model, floorOrigin, instance, 0, 0, results, new HashSet<>(), occupiedCells);

        return results;
    }

    private int findMaxId(Node<V> node, Set<Integer> visited) {
        if (node == null || visited.contains(node.id())) return -1;
        visited.add(node.id());
        int max = node.id();
        for (Node<V> neighbor : node.neighbors()) {
            max = Math.max(max, findMaxId(neighbor, visited));
        }
        return max;
    }

    private void renderGraph(Node<V> node, Pos origin, Instance instance, int xIdx, int zIdx,
                             LayoutResult[] results, Set<Integer> visited, Set<GridPos> occupiedCells) {

        if (node == null || visited.contains(node.id())) return;
        visited.add(node.id());
        occupiedCells.add(new GridPos(xIdx, zIdx));

        Pos currentPos = origin.add(xIdx * SPACING, 0, zIdx * SPACING);
        instance.setBlock(currentPos, Block.WHITE_WOOL);

        results[node.id()] = new LayoutResult(node.value(), currentPos, new NodeDisplay(node.value().toString(), currentPos));

        int attemptDir = 0;
        for (Node<V> neighbor : node.neighbors()) {
            if (!visited.contains(neighbor.id())) {
                int nextX = xIdx;
                int nextZ = zIdx;

                // Find the next available empty spot around the current node
                boolean foundSpot = false;
                while (!foundSpot && attemptDir < 100) { // Safety cap
                    int tempX = xIdx;
                    int tempZ = zIdx;

                    // Simple spiral-out or directional logic
                    switch (attemptDir % 4) {
                        case 0 -> tempX++;
                        case 1 -> tempZ++;
                        case 2 -> tempX--;
                        case 3 -> tempZ--;
                    }

                    // If we are spreading further out (diagonal/extended)
                    if (attemptDir >= 4) {
                        int multiplier = (attemptDir / 4) + 1;
                        tempX = xIdx + ((tempX - xIdx) * multiplier);
                        tempZ = zIdx + ((tempZ - zIdx) * multiplier);
                    }

                    if (!occupiedCells.contains(new GridPos(tempX, tempZ))) {
                        nextX = tempX;
                        nextZ = tempZ;
                        foundSpot = true;
                    }
                    attemptDir++;
                }

                Pos neighborPos = origin.add(nextX * SPACING, 0, nextZ * SPACING);
                drawConnection(currentPos, neighborPos, instance, Block.DIRT_PATH);

                renderGraph(neighbor, origin, instance, nextX, nextZ, results, visited, occupiedCells);
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

        double nx = -dz / length;
        double nz = dx / length;

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