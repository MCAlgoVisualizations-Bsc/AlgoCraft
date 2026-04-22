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

        // 1. Place the physical Node block (White Wool)
        instance.setBlock(currentPos, Block.WHITE_WOOL);

        // 2. Create the LayoutResult for the Label/Interactions
        // Note: Using LayoutPathProfile.create() ensures your logic is consistent
        results.add(new LayoutResult(node.value(), currentPos, new NodeDisplay(node.value().toString(), currentPos)));

        // 3. Left Branch
        if (node.left() != null) {
            Pos leftPos = currentPos.add(-spread, 0, DEPTH_SPACING);
            drawConnection(currentPos, leftPos, instance, Block.GRAY_WOOL);
            renderNode(node.left(), leftPos, instance, Math.max(1, spread / 2), results);
        }

        // 4. Right Branch
        if (node.right() != null) {
            Pos rightPos = currentPos.add(spread, 0, DEPTH_SPACING);
            drawConnection(currentPos, rightPos, instance, Block.GRAY_WOOL);
            renderNode(node.right(), rightPos, instance, Math.max(1, spread / 2), results);
        }
    }

    private void drawConnection(Pos start, Pos end, Instance instance, Block block) {
        double dist = start.distance(end);
        int steps = (int) Math.ceil(dist);

        for (int i = 1; i <= steps; i++) {
            double ratio = (double) i / steps;
            double x = start.x() + (end.x() - start.x()) * ratio;
            double z = start.z() + (end.z() - start.z()) * ratio;
            instance.setBlock(new Pos(x, start.y(), z), block);
        }
    }
}