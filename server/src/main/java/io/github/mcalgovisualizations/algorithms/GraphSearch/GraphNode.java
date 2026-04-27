package io.github.mcalgovisualizations.algorithms.GraphSearch;

import java.util.ArrayList;
import java.util.List;

public record GraphNode<V extends Comparable<V>> (
    int id,
    V value,
    List<GraphNode<V>> neighbors
) {
    public GraphNode(int id, V value) {
        this(id, value, new ArrayList<>());
    }

    public void addNeighbor(GraphNode<V> neighbor) {
        neighbors.add(neighbor);
    }
}
