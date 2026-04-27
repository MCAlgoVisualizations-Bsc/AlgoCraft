package io.github.mcalgovisualizations.algorithms.TreeSearch;

import java.util.ArrayList;
import java.util.List;

public record Node (
    int id,
    int value,
    List<Node> neighbors
) {
    public Node(int id, int value) {
        this(id, value, new ArrayList<>());
    }

    public void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }
}
