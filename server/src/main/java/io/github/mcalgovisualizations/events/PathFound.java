package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.algorithms.GraphSearch.Node;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.List;

public record PathFound(List<Node> path) implements IAlgorithmEvent {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Path Found: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).id());
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }
}
