package io.github.mcalgovisualizations.visualization.ui;

import io.github.mcalgovisualizations.visualization.PlayerControls;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Provides feedback to one or more {@link Audience} instances through sound and messages.
 * <p>
 * This class acts as a bridge between visualization controls and player-facing feedback,
 * implementing both {@link AudienceChannel} and {@link PlayerControls}.
 */
public final class PlayerFeedback implements AudienceChannel, PlayerControls {

    /**
     * The aggregated audience receiving feedback.
     */
    private final Set<Player> audiences = new HashSet<>();

    /**
     * Constructs a {@code PlayerFeedback} instance with an initial set of audiences.
     *
     * @param players the initial audiences to include
     */
    public PlayerFeedback(@NotNull final Player... players) {
        addAudience(players);
    }

    /**
     * Adds one or more audiences to the existing aggregated audience.
     *
     * @param players the audiences to add
     */
    public void addAudience(@NotNull final Player... players) {
        this.audiences.addAll(Arrays.asList(players));
    }

    public void removeAudience(@NotNull final Player... players) {
        for (var player : players) {
            this.audiences.remove(player);
        }
    }

    @Override
    public void sendActionBar(@NotNull final Component message) {
        Audience.audience(this.audiences).sendActionBar(message);
    }

    public boolean isEmpty() {
        return this.audiences.isEmpty();
    }

    /**
     * Plays a sound to the aggregated audience.
     *
     * @param key    the namespaced key of the sound (e.g., {@code minecraft:block.note_block.chime})
     * @param volume the volume of the sound
     * @param pitch  the pitch of the sound
     */
    @Override
    public void playSound(@NotNull final String key, final float volume, final float pitch) {
        Audience.audience(this.audiences).playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, volume, pitch));
    }

    /**
     * Sends a message to the aggregated audience.
     *
     * @param message the message to send
     */
    @Override
    public void sendMessage(@NotNull final Component message) {
        Audience.audience(this.audiences).sendMessage(message);
    }

    /**
     * Plays a sound indicating a randomization action.
     */
    @Override
    public void randomize() {
        playSound("minecraft:entity.item.pickup", 0.8f, 1.3f);
    }

    /**
     * Plays a sound indicating a start action.
     */
    @Override
    public void start() {
        playSound("minecraft:block.note_block.chime", 1.0f, 1.25f);
    }

    /**
     * Plays a sound indicating a stop action.
     */
    @Override
    public void pause() {
        playSound("minecraft:block.note_block.bass", 0.9f, 0.9f);
    }

    /**
     * Plays a sound indicating a step/advance action.
     */
    @Override
    public void step() {
        playSound("minecraft:block.note_block.hat", 0.6f, 1.6f);
    }

    /**
     * Plays a sound indicating a backward step action.
     */
    @Override
    public void back() {
        playSound("minecraft:block.note_block.snare", 0.7f, 1.2f);
    }

    /**
     * Handles clear/reset feedback.
     */
    @Override
    public void clear() {
        playSound("minecraft:block.note_block.amethyst", 0.7f, 0.8f);
    }

    /**
     * Plays a sound indicating a speed change.
     */
    @Override
    public int changeSpeed() {
        playSound("minecraft:block.note_block.amethyst", 0.7f, 1.2f);
        return 0;
    }
}