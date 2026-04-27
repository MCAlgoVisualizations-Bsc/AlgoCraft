package io.github.mcalgovisualizations.DataTypes;

import io.github.mcalgovisualizations.algorithms.TreeSearch.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NodeUtils {
    public static <V extends Comparable<V>> Node<V> fromList(List<V> list) {
        if (list == null || list.isEmpty()) return null;

        Node<V> root = new Node<>(0, list.get(0));
        List<Node<V>> nodes = new ArrayList<>();
        nodes.add(root);

        for (int i = 1; i < list.size(); i++) {
            Node<V> node = new Node<>(i, list.get(i));
            nodes.add(node);
            // Connect to a previous node to ensure connectivity (simulating a simple graph)
            nodes.get(ThreadLocalRandom.current().nextInt(i)).addNeighbor(node);
        }
        return root;
    }
}
