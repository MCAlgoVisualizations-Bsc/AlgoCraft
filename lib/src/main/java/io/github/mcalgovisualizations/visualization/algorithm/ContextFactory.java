package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public record ContextFactory<T, C extends AlgorithmContext<T>>(
        Function<T, C> contextCreator,
        UnaryOperator<T> copier,
        UnaryOperator<T> randomizer) {

    public C create(T sourceData) {
        T copy = copier.apply(sourceData);
        return contextCreator.apply(copy);
    }

    public T copy(T data) {
        return copier.apply(data);
    }

    public T randomize(T data) {
        return randomizer.apply(copier.apply(data));
    }
}