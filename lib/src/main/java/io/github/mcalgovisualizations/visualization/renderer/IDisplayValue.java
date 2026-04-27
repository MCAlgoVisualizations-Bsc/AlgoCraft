package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a visual element in the world that can be positioned, displayed,
 * and controlled for one or more viewers.
 *
 * <p>An {@code IDisplayValue} abstracts over concrete Minestom entities or
 * composite render objects, providing a uniform interface for positioning,
 * visibility, and basic visual state.</p>
 *
 * <p>Implementations may wrap one or more underlying entities.</p>
 */
public interface IDisplayValue {

    /**
     * Returns the current position of this display element.
     *
     * @return the world position
     */
    Pos getPos();

    /**
     * Sets the instance (world) this display belongs to.
     *
     * <p>This typically spawns or moves the underlying entity into the given instance.</p>
     *
     * @param instance the target instance
     */
    void setInstance(Instance instance);

    // TODO : Remove
    void addViewer(Player player);
    /**
     * Removes this display from the world.
     *
     * <p>This should clean up any underlying entities and free associated resources.</p>
     */
    void remove();

    /**
     * Teleports this display to a new position.
     *
     * @param pos the target position
     */
    void teleport(Pos pos);

    /**
     * Sets whether this display is visually highlighted (e.g. glowing).
     *
     * @param highlighted {@code true} to enable highlighting, {@code false} to disable
     */
    void setGlowing(boolean highlighted);

    /**
     * Returns whether this display is currently spawned in an instance.
     *
     * @return {@code true} if spawned, otherwise {@code false}
     */
    boolean isSpawned();
}
