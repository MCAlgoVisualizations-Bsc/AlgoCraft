package io.github.mcalgovisualizations.visualization.algorithms.events;

import net.kyori.adventure.text.format.NamedTextColor;

public record Message(String message, MessageType type) implements IAlgorithmEvent {
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
