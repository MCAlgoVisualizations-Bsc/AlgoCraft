package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AlgoCraftNew {
    private final Map<String, Algorithm<?, ?, ?>> algorithmRegistry = new HashMap<>();
    private final Map<UUID, AlgorithmInstance<?, ?, ?>> instances = new HashMap<>();
    private final AlgorithmUI ui = new AlgorithmUI();
    private final Instance defaultInstance;

    public AlgoCraftNew(InstanceContainer defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    public void startInstance(String id, Player... players){
        if (algorithmRegistry.get(id) == null)
            throw new IllegalArgumentException("No entry with id " + id);var entry = algorithmRegistry.get(id);
        var instance = new AlgorithmInstance<>(entry, players);
        instances.put(instance.getUUID(), instance);
        Arrays.stream(players).forEach(ui::applyRunningLayout);
    }

    public void addPlayerToInstance(UUID instanceId, Player... players) {
        if (!instances.containsKey(instanceId))
            throw new IllegalArgumentException("No instance with id " + instanceId);
        Arrays.stream(players).forEach(player -> {
            ui.applyRunningLayout(player);
            instances.get(instanceId).addPlayer(player);
        });
    }

    public void removePlayerFromInstance(Player... players) {
        instances.values().forEach(instance -> {
            instance.removePlayer(defaultInstance, players);
        });

        // dereference and clear instance if members are empty
        instances.entrySet().removeIf(entry -> {
            var algorithmInstance = entry.getValue();
            if (algorithmInstance.isEmpty()) {
                algorithmInstance.clear();
                return true;
            }
            return false;
        });
    }

    public <T, C extends AlgorithmContext<T>> void registerAlgorithm(Algorithm<T, C, ?> algorithm) {
        algorithmRegistry.put(algorithm.id(), algorithm);
    }

}
