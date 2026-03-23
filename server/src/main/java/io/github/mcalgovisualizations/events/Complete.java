package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;

public record Complete(int size) implements IAlgorithmEvent {
}
