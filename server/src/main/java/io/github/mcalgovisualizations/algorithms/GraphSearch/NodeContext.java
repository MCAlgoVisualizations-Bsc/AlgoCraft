package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.github.mcalgovisualizations.DataTypes.NodeUtils.RandomizeNode;

public class NodeContext extends AbstractContext<Node> {

    private final Random random = new Random();
    private List<Node> finalPath = new ArrayList<>();

    public NodeContext(Node values) {
        super(values);
    }

    @Override
    public NodeContext copy() {
        NodeContext copy = new NodeContext(values);
        copy.setFinalPath(new ArrayList<>(finalPath));
        return copy;
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
        this.finalPath = new ArrayList<>();
        return this.values;
    }

    public List<Node> getFinalPath() {
        return finalPath;
    }

    public void setFinalPath(List<Node> finalPath) {
        this.finalPath = finalPath;
    }
}
