package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.PlayerInsertion;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.items.VisualizationItems;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.handlers.SystemMessages;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.Arrays;

import static io.github.mcalgovisualizations.config.WorldConfig.createMainInstance;


public final class Main {
    private static AlgoCraft algo = null;

    static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();
        InstanceContainer instance = createMainInstance();

        // Sets the game time
        instance.setTimeRate(0);  // Stops time
        instance.setTime(6000);   // Sets time to noon

        algo = new AlgoCraft(instance);
        var integerCollection1 = new ArrayList<>(Arrays.asList(
                new Data<>(3),
                new Data<>(7),
                new Data<>(8),
                new Data<>(1),
                new Data<>(6),
                new Data<>(4),
                new Data<>(9),
                new Data<>(5),
                new Data<>(2)
        ));

        var integerCollection2 = new ArrayList<>(Arrays.asList(
                new Data<>(8),
                new Data<>(3),
                new Data<>(1)
        ));

        var stringCollection1 = new ArrayList<>(Arrays.asList(
                new Data<>("a"),
                new Data<>("b"),
                new Data<>("k"),
                new Data<>("x"),
                new Data<>("d"),
                new Data<>("h"),
                new Data<>("a"),
                new Data<>("b"),
                new Data<>("e")
        ));

        algo.registerAlgorithm("insertion sort (ints)", PlayerInsertion::new, integerCollection1, new FloatingLinearLayout());
        algo.registerAlgorithm("small insertion sort (ints)", PlayerInsertion::new, integerCollection2, new FloatingLinearLayout());
        algo.registerAlgorithm("insertion sort (string)", PlayerInsertion::new, stringCollection1, new FloatingLinearLayout());
        algo.addListeners(MinecraftServer.getGlobalEventHandler());

        // Register visualization control listeners (item interactions)
        registerListeners(instance);
        // registerControls(instance, algo.visualizationManager);
        registerCommands(MinecraftServer.getCommandManager());

        server.start("0.0.0.0", 25565);
    }

    static void registerListeners(InstanceContainer instance) {
        final var globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Player configuration - set spawn instance and respawn point
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Player spawn - give items and assign visualization (player is now fully in the world)
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return; // Only on first spawn

            Player player = event.getPlayer();

            // Give fly access to player
            player.setAllowFlying(true);

            // Give only the algorithm selector and spawn item by default
            player.getInventory().setItemStack(0, VisualizationItems.algorithmSelectorItem());
            // player.getInventory().setItemStack(8, VisualizationItems.spawnItem());

            // Send welcome message
            SystemMessages.sendTo(player, SystemMessages.WELCOME);
            SystemMessages.sendTo(player, SystemMessages.SELECT_ALGORITHM_HINT);
        });

        // Cleanup visualization when player disconnects
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            // VisualizationManager.removeVisualization(event.getPlayer());
        });

    }



    static void registerCommands(CommandManager cm) {
        cm.register(new Greet());
        cm.register(new Teleport());
        cm.register(new Gamemode());
        cm.register(new Spawn());
        cm.register(new testCommand(algo));
    }
}