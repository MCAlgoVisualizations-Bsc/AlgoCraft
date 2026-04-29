package io.github.mcalgovisualizations.pov;

import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public interface VillagerPovSceneAdapter<T extends ISceneOps> {
    Entity cameraTarget(T scene);
    void setLocatorBarVisible(T scene, Player player, boolean visible);
}

