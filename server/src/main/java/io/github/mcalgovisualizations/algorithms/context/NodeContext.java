package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.algorithms.TreeSearch.Node;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;

import static io.github.mcalgovisualizations.DataTypes.NodeUtils.shuffleTree;

public class NodeContext<V extends Comparable<V>> extends AbstractContext<Node<V>> {
    public NodeContext(Node<V> values) {
        super(values);
    }

    @Override
    public NodeContext<V> copy() {
        return new NodeContext<>(values);
    }

    @Override
    public Node<V> copyData() {
        if (values == null) return null;
        return new Node<>(values.id(), values.value(), values.left(), values.right());
    }

    @Override
    public Node<V> randomizeData() {
        values = shuffleTree(values);
        return copyData();
    }
}
