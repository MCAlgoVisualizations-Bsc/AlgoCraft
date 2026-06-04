package io.github.mcalgovisualizations.visualization.instance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a channel for delivering feedback to an audience.
 *
 * <p>Implementations typically bridge Minestom audiences, player parties, or
 * other presentation layers that can receive messages and sounds.</p>
 */
public interface AudienceChannel {

    /**
     * Plays a sound identified by a namespaced key.
     *
     * @param key    the namespaced sound key (e.g., {@code minecraft:block.note_block.chime})
     * @param volume the volume of the sound
     * @param pitch  the pitch of the sound
     */
    void playSound(final String key, final float volume, final float pitch);

    /**
     * Sends a message to the audience.
     *
     * @param message the message to send
     */
    void sendMessage(final Component message);

    /**
     * Sends a plain-text message to the audience.
     *
     * @param message the message to send
     */
    void sendMessage(@NotNull final String message);

    /**
     * Sends a colored plain-text message to the audience.
     *
     * @param message the message to send
     * @param color the message color
     */
    void sendMessage(@NotNull final String message, final NamedTextColor color);

    /**
     * Sends an action bar message to the audience.
     *
     * @param message the message to send
     */
    void sendActionBar(final Component message);

    /**
     * Sends a plain-text action bar message.
     *
     * @param message the message to send
     */
    void sendActionBar(@NotNull final String message);

    /**
     * Sends a colored plain-text action bar message.
     *
     * @param message the message to send
     * @param color the message color
     */
    void sendActionBar(@NotNull final String message, final NamedTextColor color);
}