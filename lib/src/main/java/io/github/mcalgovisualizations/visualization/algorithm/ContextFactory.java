package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public record ContextFactory<I, C extends AlgorithmContext<I>>(
        Function<I, C> contextCreator,
        UnaryOperator<I> copier,
        UnaryOperator<I> randomizer) {

    public C create(I sourceData) {
        I copy = copier.apply(sourceData);
        return contextCreator.apply(copy);
    }

    public I copy(I data) {
        return copier.apply(data);
    }

    public I randomize(I data) {
        return randomizer.apply(copier.apply(data));
    }
}