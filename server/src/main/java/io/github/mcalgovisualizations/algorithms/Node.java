package io.github.mcalgovisualizations.algorithms;

public record Node<V extends Comparable<V>> (
    int id,
    V value,
    Node<V> left,
    Node<V> right
) {}
