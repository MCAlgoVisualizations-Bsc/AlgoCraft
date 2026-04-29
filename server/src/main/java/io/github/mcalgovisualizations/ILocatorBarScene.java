package io.github.mcalgovisualizations;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public interface ILocatorBarScene {
    void initializeLocatorBar();
    void updateLocatorBar(int activeSlot);
    void setLocatorBarVisible(Player player, boolean visible);
    void clearLocatorBar();
    Entity cameraTarget();
}

