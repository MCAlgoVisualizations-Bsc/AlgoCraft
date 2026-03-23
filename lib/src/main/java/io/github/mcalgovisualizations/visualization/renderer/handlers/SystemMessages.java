package io.github.mcalgovisualizations.visualization.renderer.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Static system/UI messages that are not tied to any specific algorithm.
 * These cover player lifecycle events (join, disconnect) and hotbar control feedback.
 *
 * Usage: SystemMessages.sendTo(player, SystemMessages.WELCOME);
 */
public final class SystemMessages {

    // ── Constants ─────────────────────────────────────────────────────────────

    public static final Component ALGORITHM_COMPLETE =
            Component.text("Algorithm complete! Use randomize to restart.", Message.MessageType.SUCCESS.color());

    public static final Component VISUALIZATION_STARTED =
            Component.text("Visualization started!", Message.MessageType.SUCCESS.color());

    public static final Component VISUALIZATION_STOPPED =
            Component.text("Visualization stopped!", Message.MessageType.ERROR.color());

    public static final Component STEP_FORWARD =
            Component.text("Stepped forward", NamedTextColor.YELLOW);

    public static final Component STEP_BACKWARD =
            Component.text("Stepped back", NamedTextColor.GOLD);

    public static final Component RANDOMIZED =
            Component.text("Values randomized!", Message.MessageType.HINT.color());

    public static final Component NO_VISUALIZATION =
            Component.text("No visualization assigned! Use the Algorithm Selector first.", Message.MessageType.ERROR.color());

    public static final Component RETURNED_TO_HUB =
            Component.text("Returned to hub!", NamedTextColor.LIGHT_PURPLE);

    public static final Component WELCOME =
            Component.text("Welcome to Algorithm Visualizations!", Message.MessageType.SUCCESS.color());

    public static final Component SELECT_ALGORITHM_HINT =
            Component.text("Right-click the Nether Star to select an algorithm to visualize!", NamedTextColor.YELLOW);

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Send a system message component to an audience. */
    public static void sendTo(Audience audience, Component message) {
        audience.sendMessage(message);
    }

    private SystemMessages() {}
}
