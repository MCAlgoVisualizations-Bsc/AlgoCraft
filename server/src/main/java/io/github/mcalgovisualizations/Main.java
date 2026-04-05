package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.visualization.AlgorithmPlacement;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.algorithms.events.Swap;
import io.github.mcalgovisualizations.visualization.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.visualization.layouts.GridLayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
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
import java.util.ArrayList;
import java.util.Arrays;

import static io.github.mcalgovisualizations.config.WorldConfig.createMainInstance;


public final class Main {
    private static final Pos HUB_SPAWN = new Pos(194, 137, -38);

    private static final AlgorithmPlacement INSERTION_INTS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));
    private static final AlgorithmPlacement INSERTION_SMALL_PLACEMENT =
            new AlgorithmPlacement(new Pos(193, 138, 132), new Pos(194.5, 139, 136));
    private static final AlgorithmPlacement INSERTION_STRINGS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));
    private static final AlgorithmPlacement ASTAR_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 200, 145), new Pos(194.5, 200, 148));
    private static final AlgorithmPlacement BFS_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(200, 200, 145), new Pos(207.5, 200, 148));
    private static final AlgorithmPlacement DFS_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(213, 200, 145), new Pos(220.5, 200, 148));
    private static final AlgorithmPlacement GREEDY_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(226, 200, 145), new Pos(233.5, 200, 148));

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

        var sortedStringCollection = new ArrayList<>(Arrays.asList(
                new Data<>("a"),
                new Data<>("b"),
                new Data<>("c")
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
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
        ));
        algo.registerAlgorithmPresentation("insertion sort (ints)", new AlgorithmPresentation(
                "Insertion Sort",
                net.minestom.server.item.Material.IRON_SWORD,
                "A simple sorting algorithm that builds",
                "the final sorted array one item at a time.",
                "Time: O(n^2) | Space: O(1)"
        ));


        algo.registerAlgorithm(
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection2)
                        .positioning(new FloatingLinearLayout(), INSERTION_SMALL_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
        ));
        algo.registerAlgorithmPresentation("small insertion sort (ints)", new AlgorithmPresentation(
                "Small Insertion Sort",
                net.minestom.server.item.Material.GOLDEN_SWORD,
                "A compact insertion-sort demo",
                "with fewer values for quick runs.",
                "Time: O(n^2) | Space: O(1)"
        ));


        algo.registerAlgorithm(
                Algorithm.<String>build(ctx -> ctx
                        .withIdentity("insertion sort (string)", PlayerInsertion::new)
                        .withData(stringCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_STRINGS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .onCompletion(d -> AnimationPlan.empty())
        ));
        algo.registerAlgorithmPresentation("insertion sort (string)", new AlgorithmPresentation(
                "Insertion Sort (Strings)",
                net.minestom.server.item.Material.BOOK,
                "Insertion-sort using string values",
                "to demonstrate generic ordering.",
                "Time: O(n^2) | Space: O(1)"
        ));


        algo.registerAlgorithm(
                Algorithm.<String>build(ctx -> ctx
                        .withIdentity("sorted insertion", PlayerInsertion::new)
                        .withData(sortedStringCollection)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                )
        );

        final int gridX = 20;
        final int gridY = 20;
        var aStarGrid = buildPathGrid(gridX, gridY);

//         algo.registerAlgorithm("insertion sort (ints)", PlayerInsertion::new, integerCollection1, new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT);
//         algo.registerAlgorithm("small insertion sort (ints)", PlayerInsertion::new, integerCollection2, new FloatingLinearLayout(), INSERTION_SMALL_PLACEMENT);
//         algo.registerAlgorithm("insertion sort (string)", PlayerInsertion::new, stringCollection1, new FloatingLinearLayout(), INSERTION_STRINGS_PLACEMENT);
//      algo.registerAlgorithm("a* pathfinding (4-way)", () -> new PlayerAStar(gridX), aStarGrid, new GridLayout(gridX), ASTAR_2D_PLACEMENT);
//         algo.registerAlgorithm("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX), aStarGrid, new GridLayout(gridX), BFS_2D_PLACEMENT);
//         algo.registerAlgorithm("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX), aStarGrid, new GridLayout(gridX), DFS_2D_PLACEMENT);
//         algo.registerAlgorithm("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX), aStarGrid, new GridLayout(gridX), GREEDY_2D_PLACEMENT);
//
        algo.registerAlgorithm(
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("a* pathfinding (4-way)", () -> new PlayerAStar(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), ASTAR_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                )
        );
        algo.registerAlgorithmPresentation("a* pathfinding (4-way)", new AlgorithmPresentation(
                "A* Pathfinding",
                net.minestom.server.item.Material.COMPASS,
                "4-way A* on a fixed 2D obstacle map",
                "Colors show open, closed, and final path.",
                "Time: O(E log V) | Space: O(V)"
        ));

        algo.registerAlgorithm(
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), BFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                )
        );
        algo.registerAlgorithmPresentation("bfs pathfinding (4-way)", new AlgorithmPresentation(
                "BFS Pathfinding",
                net.minestom.server.item.Material.RECOVERY_COMPASS,
                "4-way BFS explores breadth-first",
                "Queue-based level-by-level expansion.",
                "Time: O(V + E) | Space: O(V)"
        ));

        algo.registerAlgorithm(
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), DFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                )
        );
        algo.registerAlgorithmPresentation("dfs pathfinding (4-way)", new AlgorithmPresentation(
                "DFS Pathfinding",
                net.minestom.server.item.Material.LOOM,
                "4-way DFS explores depth-first",
                "Stack-based backtracking expansion.",
                "Time: O(V + E) | Space: O(V)"
        ));

        algo.registerAlgorithm(
                Algorithm.<Integer>build(ctx -> ctx
                        .withIdentity("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), GREEDY_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                )
        );
        algo.registerAlgorithmPresentation("greedy best-first (4-way)", new AlgorithmPresentation(
                "Greedy Best-First",
                net.minestom.server.item.Material.REDSTONE_TORCH,
                "Fast heuristic-only pathfinding",
                "Prioritizes closeness to goal, may miss optimal paths.",
                "Time: O(E log V) | Space: O(V)"
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

    private static ArrayList<Data<Integer>> buildPathGrid(int xSize, int ySize) {
        if (xSize <= 0 || ySize <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be > 0");
        }

        ArrayList<Data<Integer>> grid = new ArrayList<>(xSize * ySize);
        final int wallPercent = 30;

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                int value;
                if (x == 0 && y == 0) {
                    value = 2; // src at (0,0)
                } else if (x == xSize - 1 && y == ySize - 1) {
                    value = 3; // dst at (n,n)
                } else if (y == 0 || x == xSize - 1) {
                    // Keep one guaranteed open corridor: top row -> right column.
                    value = 0;
                } else {
                    // Deterministic pseudo-random wall placement so each size has a stable maze.
                    int noise = Math.floorMod((x * 37) + (y * 57) + (x * y * 11), 100);
                    value = noise < wallPercent ? 1 : 0;
                }
                grid.add(new Data<>(value));
            }
        }
        return grid;
    }
}