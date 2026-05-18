package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.Node;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record PathFound(List<Node> path) implements IAlgorithmEvent {
    @Override
    public @NonNull String toString() {
        StringBuilder sb = new StringBuilder("Path Found: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getID());
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }
}
