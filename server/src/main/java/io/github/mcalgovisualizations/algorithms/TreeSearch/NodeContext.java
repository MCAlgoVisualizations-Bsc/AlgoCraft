package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.ArrayList;

public class NodeContext<V extends Comparable<V>> extends AbstractContext<Node<V>> {
    public NodeContext(Node<V> values) {
        super(values);
    }

    @Override
    public NodeContext<V> copy() {
        return new NodeContext<>(values);
    }

    @Override
    public Node<V> copyData() {
        if (values == null) return null;
        return new Node<>(values.id(), values.value(), new ArrayList<>(values.neighbors()));
    }

    @Override
    public Node<V> randomizeData() {
        // Randomizing a graph is more complex than a tree, so we'll leave it for now
        return copyData();
    }
}
