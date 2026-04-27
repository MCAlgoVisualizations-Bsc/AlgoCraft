package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // TODO: it looks like it does not remove old node.
    // Something funny happens when spam randomize
    @Override
    public Node randomizeData() {
        int nodeCount = 10 + random.nextInt(6); // 10 to 15 nodes
        List<Node> allNodes = new ArrayList<>();

        // 1. Create all nodes with random values
        for (int i = 0; i < nodeCount; i++) {
            int val = random.nextInt(100);
            allNodes.add(new Node(i, val, new ArrayList<>()));
        }

        // 2. Ensure Connectivity (Growth Algorithm)
        // Start with node 0 and connect a random "new" node to a random "connected" node
        List<Node> connected = new ArrayList<>();
        connected.add(allNodes.getFirst());

        for (int i = 1; i < nodeCount; i++) {
            Node newNode = allNodes.get(i);
            Node target = connected.get(random.nextInt(connected.size()));

            // Add bidirectional neighbors so the villager can walk back and forth
            target.neighbors().add(newNode);
            newNode.neighbors().add(target);

            connected.add(newNode);
        }

        // 3. Add Extra Edges (Make it a Graph, not just a Tree)
        int extraEdges = 3 + random.nextInt(3);
        for (int i = 0; i < extraEdges; i++) {
            Node a = allNodes.get(random.nextInt(nodeCount));
            Node b = allNodes.get(random.nextInt(nodeCount));

            // Prevent self-loops and duplicate edges
            if (a != b && !a.neighbors().contains(b)) {
                a.neighbors().add(b);
                b.neighbors().add(a);
            }
        }

        this.values = allNodes.getFirst(); // Root is always Node 0
        return this.values;
    }
}