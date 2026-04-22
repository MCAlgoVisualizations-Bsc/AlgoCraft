package io.github.mcalgovisualizations.algorithms;

public record Node<V extends Comparable<V>> (
    V value,
    Node<V> left,
    Node<V> right
) {}
