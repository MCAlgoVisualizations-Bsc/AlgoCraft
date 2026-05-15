package io.github.mcalgovisualizations.config;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import java.util.function.Consumer;

// Can this be serialized and saved 🧐
public class PlayerConfig implements Consumer<AsyncPlayerConfigurationEvent> {
    private final InstanceContainer _spawnInstance;

    public PlayerConfig(InstanceContainer instanceContainer) {
        _spawnInstance = instanceContainer;
    }

    @Override
    public void accept(AsyncPlayerConfigurationEvent event) {
        var player = event.getPlayer();

        // 1. Set Gamemode when spawned
        player.setGameMode(GameMode.CREATIVE);

        // 2. Set Instance
        event.setSpawningInstance(_spawnInstance);

        // 3. Set Respawn Point
        player.setRespawnPoint(new Pos(0.0, 42.0, 0.0));
    }
}