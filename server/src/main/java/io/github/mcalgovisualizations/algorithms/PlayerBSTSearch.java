package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.NodeContext;
import io.github.mcalgovisualizations.events.NodeCompare;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

public final class PlayerBSTSearch<I extends Comparable<I>> implements IPlayerSort<NodeContext<I>> {

    @Override
    public void run(NodeContext<I> context) {
        Node<I> root = context.values;
        if (root == null) {
            return;
        }

        // For visualization, let's pick a target value.
        // In a real search, this might come from the context or user input.
        // For now, let's assume we are searching for the root's value just as a placeholder.
        I targetValue = root.value();

        Node<I> cursor = root;

        while (cursor != null) {
            // Emit a comparison event so the UI highlights the current node
            // Note: You may need to adjust the Compare event parameters to fit your Node structure
            context.emit(new NodeCompare<>(cursor, root));

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
}