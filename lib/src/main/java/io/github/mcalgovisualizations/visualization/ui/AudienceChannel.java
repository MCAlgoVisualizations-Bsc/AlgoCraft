package io.github.mcalgovisualizations.visualization.ui;

import net.kyori.adventure.text.Component;

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
    void playSound(String key, float volume, float pitch);

    /**
     * Sends a message to the audience.
     *
     * @param message the message to send
     */
    void sendMessage(Component message);

    /**
     * Sends an action bar message to the audience.
     * @param message the message to send
     */
    void sendActionBar(Component message);
}