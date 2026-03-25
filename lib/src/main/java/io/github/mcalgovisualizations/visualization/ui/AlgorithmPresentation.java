package io.github.mcalgovisualizations.visualization.ui;

import net.minestom.server.item.Material;

public record AlgorithmPresentation(
        String displayName,
        Material icon,
        String description1,
        String description2,
        String complexity
) {
    public static AlgorithmPresentation fallback(String algorithmId) {
        return new AlgorithmPresentation(
                algorithmId,
                Material.STICK,
                "",
                "",
                ""
        );
    }
}

