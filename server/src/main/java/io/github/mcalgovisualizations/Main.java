package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.PlayerInsertion;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.visualization.AlgorithmPlacement;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.algorithms.events.Swap;
import io.github.mcalgovisualizations.visualization.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.handlers.CompareHandler;
import io.github.mcalgovisualizations.visualization.renderer.handlers.NoOpHandler;
import io.github.mcalgovisualizations.visualization.renderer.handlers.SwapHandler;
import io.github.mcalgovisualizations.visualization.renderer.handlers.SystemMessages;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.mcalgovisualizations.config.WorldConfig.createMainInstance;


public final class Main {
    private static final Pos HUB_SPAWN = new Pos(194, 137, -38);

    private static final AlgorithmPlacement INSERTION_INTS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));
    private static final AlgorithmPlacement INSERTION_SMALL_PLACEMENT =
            new AlgorithmPlacement(new Pos(193, 138, 132), new Pos(194.5, 139, 136));
    private static final AlgorithmPlacement INSERTION_STRINGS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));

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

        algo.registerAlgorithm(
                Algorithm.<String>register(ctx -> ctx
                        .identify("insertion sort (ints)", PlayerInsertion::new)
                        .withData(stringCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
        ));
        algo.registerAlgorithm(
                Algorithm.<Integer>register(ctx -> ctx
                        .identify("small insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection2)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
        ));
        algo.registerAlgorithm(
                Algorithm.<String>register(ctx -> ctx
                        .identify("insertion sort (string)", PlayerInsertion::new)
                        .withData(stringCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
        ));

        //How the UI Looks
        var myData = List.of(new Data<>(8), new Data<>(3), new Data<>(1));


        algo.registerAlgorithmPresentation("insertion sort (ints)", new AlgorithmPresentation(
                "Insertion Sort",
                net.minestom.server.item.Material.IRON_SWORD,
                "A simple sorting algorithm that builds",
                "the final sorted array one item at a time.",
                "Time: O(n^2) | Space: O(1)"
        ));
        algo.registerAlgorithmPresentation("small insertion sort (ints)", new AlgorithmPresentation(
                "Small Insertion Sort",
                net.minestom.server.item.Material.GOLDEN_SWORD,
                "A compact insertion-sort demo",
                "with fewer values for quick runs.",
                "Time: O(n^2) | Space: O(1)"
        ));
        algo.registerAlgorithmPresentation("insertion sort (string)", new AlgorithmPresentation(
                "Insertion Sort (Strings)",
                net.minestom.server.item.Material.BOOK,
                "Insertion-sort using string values",
                "to demonstrate generic ordering.",
                "Time: O(n^2) | Space: O(1)"
        ));
        algo.setSpawnAction(player -> player.teleport(HUB_SPAWN));
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
            player.setRespawnPoint(HUB_SPAWN);
        });

        // Player spawn - give items and assign visualization (player is now fully in the world)
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return; // Only on first spawn

            Player player = event.getPlayer();

            // Give fly access to player
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlying(true);

            // Library UI owns default hotbar layout (selector + spawn item)
            algo.applyDefaultLayout(player);

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
    }
}