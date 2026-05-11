package io.github.mcalgovisualizations.algorithms.GraphSearch;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    private final int id;
    private final int value;
    private List<Node> neighbors = new ArrayList<>();
    private @NonNull NodeTarget status = NodeTarget.None;

    public Node(int id, int value, @NonNull NodeTarget status) {
        this.id = id;
        this.value = value;
        this.status = status;
    }

    public Node(int id, int value, List<Node> neighbors) {
        this.id = id;
        this.value = value;
        this.neighbors = neighbors;
    }

    public Node(int id, int value) {
        this.id = id;
        this.value = value;
    }

    public int getID() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public @NonNull NodeTarget getStatus() {
        return status;
    }

    public void setStatus(@NonNull NodeTarget target) {
        status = target;
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

    public enum NodeTarget {
        Start,
        End,
        None
    }
}
