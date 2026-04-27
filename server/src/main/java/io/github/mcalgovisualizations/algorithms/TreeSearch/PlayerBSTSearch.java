package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.algorithms.context.NodeContext;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public final class PlayerBSTSearch<I extends Comparable<I>> implements IPlayerSort<NodeContext<I>> {

    @Override
    public void run(NodeContext<I> context) {
        Node<I> root = context.values;
        if (root == null) {
            return;
        }

        // For visualization, let's pick a target value.
        I targetValue = findSomeValue(root);

        Node<I> cursor = root;

        while (cursor != null) {
            // Emit a comparison event. 
            // x: current node being visited
            // y: -1 (no physical slot for the target value)
            // xValue: value of current node
            // yValue: the target value we are searching for
            context.emit(new Compare(cursor.id(), -1, cursor.value(), targetValue));

            int cmp = targetValue.compareTo(cursor.value());

            if (cmp == 0) {
                // Found the node!
                return;
            }

            // Standard BST traversal
            if (cmp < 0) {
                cursor = cursor.left();
            } else {
                cursor = cursor.right();
            }
        }
    }

    private I findSomeValue(Node<I> node) {
        // Try to find a leaf or something deep to make the search interesting
        if (node.left() != null && Math.random() > 0.5) return findSomeValue(node.left());
        if (node.right() != null) return findSomeValue(node.right());
        if (node.left() != null) return findSomeValue(node.left());
        return node.value();
    }

}
