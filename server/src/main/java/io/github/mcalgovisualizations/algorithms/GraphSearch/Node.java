package io.github.mcalgovisualizations.algorithms.GraphSearch;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public @NonNull String toString() {
        return "Node{id=" + id + ", value=" + value + "}";
    }
}
