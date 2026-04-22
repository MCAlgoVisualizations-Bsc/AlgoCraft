package io.github.mcalgovisualizations.algorithms.context;

import io.github.mcalgovisualizations.algorithms.Node;
import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;

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
        return new Node<>(values.value(), values.left(), values.right());
    }

    @Override
    public Node<V> randomizeData() {
        values = shuffleTree(values);
        return copyData();
    }
}
