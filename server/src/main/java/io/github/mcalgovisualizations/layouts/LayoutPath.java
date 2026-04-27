package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.algorithms.Node;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public class LayoutPath implements ILayout<Node<Integer>> {

    private static final int INITIAL_HORIZONTAL_SPREAD = 16;
    private static final int DEPTH_SPACING = 6;

    public LayoutPath() {}

    @Override
    public LayoutResult[] compute(Node<Integer> model, Pos origin, Instance instance) {
        if (model == null) return new LayoutResult[0];

        List<LayoutResult> results = new ArrayList<>();
        double floorY = Math.floor(origin.y()) - 1;
        Pos floorOrigin = new Pos(origin.x(), floorY, origin.z());

        renderNode(model, floorOrigin, instance, INITIAL_HORIZONTAL_SPREAD, results);

        return results.toArray(new LayoutResult[0]);
    }

    private void renderNode(Node<Integer> node, Pos currentPos, Instance instance, int spread, List<LayoutResult> results) {
        if (node == null) return;

        // Place the node block (using White Wool as requested, or you could swap to Path here too)
        instance.setBlock(currentPos, Block.WHITE_WOOL);

        results.add(new LayoutResult(node.value(), currentPos, new NodeDisplay(node.value().toString(), currentPos)));

        if (node.left() != null) {
            Pos leftPos = currentPos.add(-spread, 0, DEPTH_SPACING);
            // Updated to use Dirt Path
            drawConnection(currentPos, leftPos, instance, Block.DIRT_PATH);
            renderNode(node.left(), leftPos, instance, Math.max(1, spread / 2), results);
        }

        if (node.right() != null) {
            Pos rightPos = currentPos.add(spread, 0, DEPTH_SPACING);
            // Updated to use Dirt Path
            drawConnection(currentPos, rightPos, instance, Block.DIRT_PATH);
            renderNode(node.right(), rightPos, instance, Math.max(1, spread / 2), results);
        }
    }

    private void drawConnection(Pos start, Pos end, Instance instance, Block block) {
        double dist = start.distance(end);
        int steps = (int) Math.ceil(dist);

        // Calculate the direction vector of the path
        double dx = end.x() - start.x();
        double dz = end.z() - start.z();

        // Calculate the perpendicular (normal) vector for the width
        // For a 2D plane (x, z), the perpendicular of (x, z) is (-z, x)
        double length = Math.sqrt(dx * dx + dz * dz);
        double nx = -dz / length;
        double nz = dx / length;

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            double centerX = start.x() + dx * ratio;
            double centerZ = start.z() + dz * ratio;

            // Offset by -1, 0, and 1 to create a width of 3
            for (int offset = -1; offset <= 1; offset++) {
                double finalX = centerX + (nx * offset);
                double finalZ = centerZ + (nz * offset);

                // We round to ensure we hit block coordinates properly
                instance.setBlock(new Pos(Math.round(finalX), start.y(), Math.round(finalZ)), block);
            }
        }
    }
}