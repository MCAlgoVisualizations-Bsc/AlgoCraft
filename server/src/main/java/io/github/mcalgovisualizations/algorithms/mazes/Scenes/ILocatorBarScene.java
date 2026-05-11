package io.github.mcalgovisualizations.algorithms.mazes.Scenes;

import net.minestom.server.entity.Player;

/**
 * Server-owned scene capability for pathfinding locator boss bars.
 */
public interface ILocatorBarScene {
    void initializeLocatorBar();

    void updateLocatorBar(int activeSlot);

    void setLocatorBarVisible(Player player, boolean visible);

    void clearLocatorBar();
}

