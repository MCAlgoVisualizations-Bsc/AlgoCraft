package io.github.mcalgovisualizations.Villager;

import io.github.mcalgovisualizations.algorithms.GraphSearch.GraphScene;
import io.github.mcalgovisualizations.algorithms.mazes.Scenes.GridScene;
import io.github.mcalgovisualizations.algorithms.mazes.Scenes.HeuristicGridScene;
import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.visualization.instance.AlgorithmInstance;
import io.github.mcalgovisualizations.visualization.instance.PlayerFeedback;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public final class VillagerPovManager {

    private final Map<Class<? extends ISceneOps>, VillagerPovSceneAdapter<?>> adapters = new LinkedHashMap<>();
    private final Set<UUID> enabledPlayers = new HashSet<>();
    private final AlgoCraft algo;

    public VillagerPovManager(AlgoCraft algo) {
        this.algo = algo;

        adapters.put(HeuristicGridScene.class, new VillagerPovSceneAdapter<HeuristicGridScene>() {
            @Override
            public Entity cameraTarget(HeuristicGridScene scene) { return scene.cameraTarget(); }
            @Override
            public void setLocatorBarVisible(HeuristicGridScene scene, Player player, boolean visible) {
                scene.setLocatorBarVisible(player, visible);
            }
        });

        adapters.put(GridScene.class, new VillagerPovSceneAdapter<GridScene>() {
            @Override
            public Entity cameraTarget(GridScene scene) { return scene.cameraTarget(); }
        });

        adapters.put(GraphScene.class, new VillagerPovSceneAdapter<GraphScene>() {
            @Override
            public Entity cameraTarget(GraphScene scene) { return scene.cameraTarget(); }
        });
    }

    public boolean toggle(Player player, ISceneOps scene, PlayerFeedback audience) {
        if (enabledPlayers.contains(player.getUuid())) {
            disable(player, scene);
            return true;
        }

        VillagerPovSceneAdapter adapter = resolveAdapter(scene);
        if (adapter == null) return false;

        Entity cameraTarget = adapter.cameraTarget(scene);
        if (cameraTarget == null || !cameraTarget.isActive()) {
            audience.sendMessage(Component.text("Villager POV unavailable (no active villager)", NamedTextColor.GRAY));
            return false;
        }

        // Mount player to the villager
        player.setInvisible(true);
        cameraTarget.addPassenger(player);

        // Show the locator bar if the adapter supports it
        adapter.setLocatorBarVisible(scene, player, true);

        enabledPlayers.add(player.getUuid());
        return true;
    }

    public void disable(Player player, ISceneOps scene) {
        if (!enabledPlayers.remove(player.getUuid())) return;

        VillagerPovSceneAdapter adapter = resolveAdapter(scene);
        if (adapter != null) {
            adapter.setLocatorBarVisible(scene, player, false);
        }

        if (player.getVehicle() != null) {
            player.getVehicle().removePassenger(player);
        }
        player.setInvisible(false);
    }

    @SuppressWarnings("unchecked")
    private VillagerPovSceneAdapter resolveAdapter(ISceneOps scene) {
        if (scene == null) return null;
        for (var entry : adapters.entrySet()) {
            if (entry.getKey().isInstance(scene)) return entry.getValue();
        }
        return null;
    }

    public Optional<ISceneOps> resolveScene(Player player) {
        return algo.sceneFor(player);
    }

    public interface VillagerPovSceneAdapter<S extends ISceneOps> {
        Entity cameraTarget(S scene);
        default void setLocatorBarVisible(S scene, Player player, boolean visible) {}
    }
}