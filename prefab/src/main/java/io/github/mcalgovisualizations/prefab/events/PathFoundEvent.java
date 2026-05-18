package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.Node;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;

import java.util.Map;

public record PathFoundEvent(
        Node startNode,
        Node targetNode,
        Map<Integer, Node> parents
) implements IAlgorithmEvent {}
