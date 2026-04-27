package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.github.mcalgovisualizations.DataTypes.NodeUtils.RandomizeNode;

public class NodeContext extends AbstractContext<Node> {

    private final Random random = new Random();

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
        // Basic copy of the root. Note: In a graph, true deep copying
        // usually requires a Map to handle cycles.
        return new Node(values.id(), values.value(), new ArrayList<>(values.neighbors()));
    }

    @Override
    public Node randomizeData() {
        this.values = RandomizeNode();
        return this.values;
    }
}