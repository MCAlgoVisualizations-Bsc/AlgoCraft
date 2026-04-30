package io.github.mcalgovisualizations.visualization.instance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a channel for delivering feedback to an audience.
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

    void sendMessage(@NotNull final String message);

    void sendMessage(@NotNull final String message, final NamedTextColor color);

    /**
     * Sends an action bar message to the audience.
     * @param message the message to send
     */
    void sendActionBar(final Component message);

    void sendActionBar(@NotNull final String message);

    void sendActionBar(@NotNull final String message, final NamedTextColor color);
}