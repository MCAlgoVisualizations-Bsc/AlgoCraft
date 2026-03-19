package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

import java.util.*;

/**
 * Manages visualizations for all players.
 * Each player can have their own active visualization instance.
 */
public class VisualizationManager {
    private final Map<String, Pos> areaLocations = new HashMap<>();
    private final Map<UUID, VisualizationController> playerSteppers = new HashMap<>();

    public VisualizationManager(){
        // Define default area locations for different visualization types
        areaLocations.put("sorting", new Pos(5, 42, 5));
        areaLocations.put("pathfinding", new Pos(-100, 42, 0));
        areaLocations.put("trees", new Pos(0, 42, 100));
        areaLocations.put("bfs", new Pos(50, 42, 50));
    }

    public void defineWorkArea(String algorithmType, Pos pos) {
        areaLocations.put(algorithmType, pos);
    }

    /**
     * Assign a visualization to a player based on the type.
     * Cleans up any existing visualization for the player.
     *
     * @param player   The player to assign the visualization to
     * @param type     The type of visualization (e.g., "sorting", "insertionsort")
     * @param instance The game instance
     */
    public void assignVisualization(
            Player player,
            String type, // Todo - fix this so that it's not a string and either determined by the lib or the user.
            InstanceContainer instance,
            SortingCollection<?> collection,
            IPlayerSort playerAlgorithm
    ) {
        // Clean up existing visualization
        removeVisualization(player);

        final ILayout layout = new FloatingLinearLayout();
        final var origin = getAreaLocation("sorting");

        final var renderer = new Renderer(instance, origin, layout);
        final var controller = new VisualizationController(playerAlgorithm, renderer, collection);
        controller.setAudience(player);

        controller.startVisualization();

        playerSteppers.put(player.getUuid(), controller);
    }

    /**
     * Get the current visualization for a player.
     *
     * @param player The player
     * @return The visualization, or null if none assigned
     */
    public VisualizationController getVisualization(Player player) {
        return playerSteppers.get(player.getUuid());
    }

    /**
     * Remove and cleanup the visualization for a player.
     *
     * @param player The player
     */
    public void removeVisualization(Player player) {
        VisualizationController vis = playerSteppers.remove(player.getUuid());
        if (vis != null) {
            vis.cleanup();
        }
    }

    /**
     * Get the location of a visualization area.
     *
     * @param area The area name
     * @return The position, or null if not found
     */
    public Pos getAreaLocation(String area) {
        return areaLocations.get(area.toLowerCase());
    }
}