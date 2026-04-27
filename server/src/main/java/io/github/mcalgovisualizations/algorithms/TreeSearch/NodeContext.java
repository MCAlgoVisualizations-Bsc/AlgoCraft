package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import java.util.ArrayList;

public class NodeContext extends AbstractContext<Node> {
    public NodeContext(Node values) {
        super(values);
    }

    @Override
    public NodeContext copy() {
        return new NodeContext(values);
    }

    @Override
    public Node copyData() {
        if (values == null) return null;
        return new Node(values.id(), values.value(), new ArrayList<>(values.neighbors()));
    }

    @Override
    public Node randomizeData() {
        // Randomizing a graph is more complex than a tree, so we'll leave it for now
        return copyData();
    }
}
