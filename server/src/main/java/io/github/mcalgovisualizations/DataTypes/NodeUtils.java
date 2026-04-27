package io.github.mcalgovisualizations.DataTypes;

import io.github.mcalgovisualizations.algorithms.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeUtils {
    public static <V extends Comparable<V>> Node<V> shuffleTree(Node<V> root) {
        List<V> values = new ArrayList<>();
        collectValues(root, values);
        Collections.shuffle(values);

        Node<V> newRoot = null;
        for (int i = 0; i < values.size(); i++) {
            newRoot = insert(newRoot, values.get(i), i);
        }
        return newRoot;
    }

    public static <V extends Comparable<V>> Node<V> fromList(List<V> list) {
        if (list == null || list.isEmpty()) return null;

        Node<V> root = null;
        for (int i = 0; i < list.size(); i++) {
            root = insert(root, list.get(i), i);
        }
        return root;
    }

    private static <V extends Comparable<V>> void collectValues(Node<V> node, List<V> list) {
        if (node == null) return;
        list.add(node.value());
        collectValues(node.left(), list);
        collectValues(node.right(), list);
    }

    private static <V extends Comparable<V>> Node<V> insert(Node<V> node, V value, int id) {
        if (node == null) return new Node<>(id, value, null, null);

        if (value.compareTo(node.value()) < 0) {
            return new Node<>(node.id(), node.value(), insert(node.left(), value, id), node.right());
        } else {
            return new Node<>(node.id(), node.value(), node.left(), insert(node.right(), value, id));
        }
    }
}
