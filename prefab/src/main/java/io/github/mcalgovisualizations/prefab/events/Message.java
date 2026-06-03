package io.github.mcalgovisualizations.prefab.events;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Event that carries a text message for the visualization.
 *
 * @param message the message text
 * @param type the semantic type of the message
 */
public record Message(String message, MessageType type) implements IAlgorithmEvent {
    /**
     * Semantic message categories used by the prefab handlers.
     */
    public enum MessageType {
        INFO(NamedTextColor.GRAY),
        ERROR(NamedTextColor.RED),
        SUCCESS(NamedTextColor.GREEN),
        HINT(NamedTextColor.AQUA);

        private final NamedTextColor color;

        MessageType(NamedTextColor color) {
            this.color = color;
        }

        public NamedTextColor color() {
            return color;
        }

    }
}
