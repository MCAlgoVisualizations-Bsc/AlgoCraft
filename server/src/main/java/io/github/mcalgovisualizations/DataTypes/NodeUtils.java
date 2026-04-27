package io.github.mcalgovisualizations.DataTypes;

import io.github.mcalgovisualizations.algorithms.TreeSearch.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NodeUtils {
    public static Node fromList(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;

        Node root = new Node(0, list.getFirst());
        List<Node> nodes = new ArrayList<>();
        nodes.add(root);

        for (int i = 1; i < list.size(); i++) {
            Node node = new Node(i, list.get(i));
            nodes.add(node);
            // Connect to a previous node to ensure connectivity (simulating a simple graph)
            nodes.get(ThreadLocalRandom.current().nextInt(i)).addNeighbor(node);
        }
        return root;
    }
}
