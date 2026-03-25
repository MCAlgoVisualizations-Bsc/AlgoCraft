package io.github.mcalgovisualizations.visualization.ui;

@FunctionalInterface
public interface AlgorithmPresentationProvider {
    AlgorithmPresentation presentationFor(String algorithmId);
}

