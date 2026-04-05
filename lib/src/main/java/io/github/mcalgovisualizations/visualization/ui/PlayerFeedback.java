package io.github.mcalgovisualizations.visualization.ui;

import io.github.mcalgovisualizations.visualization.PlayerControls;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides feedback to one or more {@link Audience} instances through sound and messages.
 * <p>
 * This class acts as a bridge between visualization controls and player-facing feedback,
 * implementing both {@link AudienceChannel} and {@link PlayerControls}.
 * It aggregates multiple audiences into a single composite audience.
 */
public final class PlayerFeedback implements AudienceChannel, PlayerControls {

    /**
     * The aggregated audience receiving feedback.
     */
    private Audience audience = Audience.empty();

    /**
     * Constructs a {@code PlayerFeedback} instance with an initial set of audiences.
     *
     * @param audience the initial audiences to include
     */
    public PlayerFeedback(@NotNull final Audience... audience) {
        addAudience(audience);
    }

    /**
     * Adds one or more audiences to the existing aggregated audience.
     *
     * @param audience the audiences to add
     */
    public void addAudience(@NotNull final Audience... audience) {
        final var newAudience = new ArrayList<>(List.of(audience));
        final var _ = newAudience.add(this.audience);
        this.audience = Audience.audience(newAudience);
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
        this.audience.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, volume, pitch));
    }

    /**
     * Sends a message to the aggregated audience.
     *
     * @param message the message to send
     */
    @Override
    public void sendMessage(@NotNull final Component message) {
        audience.sendMessage(message);
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
        playSound("minecraft:block.note_block.Amethyst", 0.7f, 1.2f);
    }

}