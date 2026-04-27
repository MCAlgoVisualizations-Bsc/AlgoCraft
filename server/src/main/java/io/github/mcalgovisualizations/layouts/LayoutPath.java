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

public class LayoutPath<V extends Comparable<V>> implements ILayout<Node<V>> {

    private static final int INITIAL_HORIZONTAL_SPREAD = 16;
    private static final int DEPTH_SPACING = 6;

    public LayoutPath() {}

    @Override
    public LayoutResult[] compute(Node<V> model, Pos origin, Instance instance) {
        if (model == null) return new LayoutResult[0];

        int maxId = findMaxId(model);
        LayoutResult[] results = new LayoutResult[maxId + 1];

        double floorY = Math.floor(origin.y()) - 1;
        Pos floorOrigin = new Pos(origin.x(), floorY, origin.z());

        renderNode(model, floorOrigin, instance, INITIAL_HORIZONTAL_SPREAD, results);

        return results;
    }

    private int findMaxId(Node<V> node) {
        if (node == null) return -1;
        return Math.max(node.id(), Math.max(findMaxId(node.left()), findMaxId(node.right())));
    }

    private void renderNode(Node<V> node, Pos currentPos, Instance instance, int spread, LayoutResult[] results) {
        if (node == null) return;

        // Place the node block
        instance.setBlock(currentPos, Block.WHITE_WOOL);

        results[node.id()] = new LayoutResult(node.value(), currentPos, new NodeDisplay(node.value().toString(), currentPos));

        if (node.left() != null) {
            Pos leftPos = currentPos.add(-spread, 0, DEPTH_SPACING);
            drawConnection(currentPos, leftPos, instance, Block.DIRT_PATH);
            renderNode(node.left(), leftPos, instance, Math.max(1, spread / 2), results);
        }

        if (node.right() != null) {
            Pos rightPos = currentPos.add(spread, 0, DEPTH_SPACING);
            drawConnection(currentPos, rightPos, instance, Block.DIRT_PATH);
            renderNode(node.right(), rightPos, instance, Math.max(1, spread / 2), results);
        }
    }

    private void drawConnection(Pos start, Pos end, Instance instance, Block block) {
        double dist = start.distance(end);
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
