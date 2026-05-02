package io.github.mcalgovisualizations.pov;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.handlers.VillagerPovHandler;
import io.github.mcalgovisualizations.GridScene;
import io.github.mcalgovisualizations.CaveTunnelScene;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

public final class PovManager {
    public static void setup(AlgoCraft algo, InstanceContainer instance) {
        SceneAdapterRegistry registry = new SceneAdapterRegistry();

        registry.register(GridScene.class, new VillagerPovSceneAdapter<GridScene>() {
            @Override
            public Entity cameraTarget(GridScene scene) {
                return scene.cameraTarget();
            }

            @Override
            public void setLocatorBarVisible(GridScene scene, Player player, boolean visible) {
                scene.setLocatorBarVisible(player, visible);
            }
        });

        registry.register(CaveTunnelScene.class, new VillagerPovSceneAdapter<CaveTunnelScene>() {
            @Override
            public Entity cameraTarget(CaveTunnelScene scene) {
                return scene.cameraTarget();
            }

            @Override
            public void setLocatorBarVisible(CaveTunnelScene scene, Player player, boolean visible) {
                scene.setLocatorBarVisible(player, visible);
            }
        });

        VillagerPovHandler povHandler = new VillagerPovHandler(registry);
        VillagerPovListenerRegistrar povRegistrar = new VillagerPovListenerRegistrar(algo, povHandler);
        povRegistrar.register(MinecraftServer.getGlobalEventHandler());
    }
}

