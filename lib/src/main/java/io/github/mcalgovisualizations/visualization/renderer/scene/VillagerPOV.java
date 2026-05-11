package io.github.mcalgovisualizations.visualization.renderer.scene;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Interface for scenes that support villager point-of-view camera control.
 *
 * <p>Scenes implementing this interface allow players to mount a villager entity
 * and view the algorithm execution from the villager's perspective.</p>
 */
public interface VillagerPOV {

    /**
     * Returns the entity to be used as the camera target.
     *
     * <p>The player will be mounted to this entity when POV is activated.</p>
     *
     * @return the entity to ride, or null if POV is unavailable
     */
    Entity cameraTarget();

    /**
     * Controls visibility of auxiliary UI elements (e.g., heuristic distance bar).
     *
     * <p>Default implementation does nothing. Override to show/hide POV-related UI.</p>
     *
     * @param player the player whose UI should be updated
     * @param visible true to show, false to hide
     */
    default void setLocatorBarVisible(Player player, boolean visible) {
        // No-op by default
    }

    /**
     * Called when POV mode is toggled on or off.
     *
     * <p>Default implementation does nothing. Override for custom POV setup/teardown.</p>
     *
     * @param player the player toggling POV
     * @param enabled true if POV was activated, false if deactivated
     */
    default void onPovToggle(Player player, boolean enabled) {
        // No-op by default
    }
}

