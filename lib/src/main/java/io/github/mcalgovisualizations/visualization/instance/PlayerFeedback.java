package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.engine.PlayerControls;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


/**
 * Provides feedback to one or more {@link Audience} instances through sound and messages.
 * <p>
 * This class acts as a bridge between visualization controls and player-facing feedback,
 * implementing both {@link AudienceChannel} and {@link PlayerControls}.
 */
public final class PlayerFeedback implements AudienceChannel, PlayerControls {

    private final Supplier<Audience> supplier;
    private Audience audience() { return this.supplier.get(); }

    public PlayerFeedback(@NotNull Supplier<Audience> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void sendActionBar(@NotNull final Component message) {
        audience().sendActionBar(message);
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
        audience().playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, volume, pitch));
    }

    /**
     * Sends a message to the aggregated audience.
     *
     * @param message the message to send
     */
    @Override
    public void sendMessage(@NotNull final Component message) {
        audience().sendMessage(message);
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