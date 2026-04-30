package io.github.mcalgovisualizations.visualization.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a visual representation of an algorithm.
 * @param algorithmId the algorithm's unique identifier
 * @param icon the algorithm's icon
 * @param complexity the algorithm's complexity
 * @param description a description of the algorithm
 */
public record AlgorithmPresentation(
        @NotNull String algorithmId,
        Material icon,
        String complexity,
        String... description
) {
    public AlgorithmPresentation(String algorithmId) {
        this(algorithmId, Material.STICK, "", "");
    }

    private static final Component WHITE_SPACE = Component.empty();

    public List<Component> getComponents() {
        List<Component> parsedDescription = Stream.of(description)
                .filter(desc -> !desc.isBlank())
                .map(desc -> {
                    desc = desc.trim();
                    return (Component) Component.text(desc, NamedTextColor.GRAY);
                })
                .toList();

        final var lore = new ArrayList<>(parsedDescription);
        final var complexityComponent = Component.text(complexity, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false);
        final var hintComponent = Component.text("Click to select!", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false);

        if(!complexity.isBlank()) {
            lore.add(WHITE_SPACE);
            lore.add(complexityComponent);
        }

        lore.add(WHITE_SPACE);
        lore.add(hintComponent);
        return lore;
    }

    public Component getCustomName() {
        return Component.text(algorithmId, NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false);
    }
}

