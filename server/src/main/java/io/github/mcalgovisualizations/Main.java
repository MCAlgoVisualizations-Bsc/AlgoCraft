package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.config.MapConstants;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.visualization.SystemMessages;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.algorithms.events.Swap;
import io.github.mcalgovisualizations.visualization.layouts.BSTNodeLayout;
import io.github.mcalgovisualizations.visualization.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.visualization.layouts.GridLayout;
import io.github.mcalgovisualizations.visualization.layouts.TSTNodeLayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.GridScene;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.github.mcalgovisualizations.config.MapConstants.*;
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

        var bstCollection = new ArrayList<>(Arrays.asList(
                new Data<>("a"),
                new Data<>("b"),
                new Data<>("k"),
                new Data<>("x"),
                new Data<>("d"),
                new Data<>("h"),
                new Data<>("a"),
                new Data<>("b"),
                new Data<>("e"),
                new Data<>("h"),
                new Data<>("s"),
                new Data<>("j"),
                new Data<>("s"),
                new Data<>("v"),
                new Data<>("k")
        ));





        algo.registerAlgorithm(
                Algorithm.<Integer, ISceneOps>build(ctx -> ctx
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort",
                                Material.IRON_SWORD,
                                "A simple sorting algorithm that builds",
                                "the final sorted array one item at a time.",
                                "Time: O(n^2) | Space: O(1)"
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
        ));


        algo.registerAlgorithm(
                Algorithm.<Integer, ISceneOps>build(ctx -> ctx
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection2)
                        .positioning(new FloatingLinearLayout(), INSERTION_SMALL_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Small Insertion Sort",
                                Material.GOLDEN_SWORD,
                                "A compact insertion-sort demo",
                                "with fewer values for quick runs.",
                                "Time: O(n^2) | Space: O(1)"
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
        ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("insertion sort (string)", PlayerInsertion::new)
                        .withData(stringCollection1)
                        .positioning(new FloatingLinearLayout(), INSERTION_STRINGS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .onCompletion(_ -> AnimationPlan.empty())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort (Strings)",
                                Material.BOOK,
                                "Insertion-sort using string values",
                                "to demonstrate generic ordering.",
                                "Time: O(n^2) | Space: O(1)"
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
        ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("sorted insertion", PlayerInsertion::new)
                        .withData(sortedStringCollection)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("bst search", PlayerBSTSearch::new)
                        .withData(bstCollection)
                        .positioning(new BSTNodeLayout(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Binary Search Tree (Search)",
                                Material.SPYGLASS,
                                "Builds a BST from the current values",
                                "then searches for one value using branch decisions.",
                                "Tip: use Randomize before Start to explore new search paths"
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        var unordered_tree_search_data = new ArrayList<>(bstCollection);
        Collections.shuffle(unordered_tree_search_data);

        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("unordered_tree_search", PlayerUnorderedTree::new)
                        .withData(unordered_tree_search_data)
                        // Use the new Unordered Layout to ensure Root is at index 0 (the top)
                        .positioning(new UnorderedTreeLayout(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Unordered Binary Tree (Linear Search)",
                                Material.DARK_OAK_LOG,
                                "A tree filled level-by-level.",
                                "Search must visit nodes in order",
                                "until the target is found."
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("tst search", PlayerTSTSearch::new)
                        .withData(stringCollection1)
                        .positioning(new TSTNodeLayout(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Ternary Search Tree (Search)",
                                Material.SPYGLASS,
                                "Builds a ternary search tree from the current strings",
                                "then searches for one value using left, middle, and right branches.",
                                "Tip: equal matches follow the middle branch"
                        ))
                        .withScene(sceneContext -> new DefaultScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        final int gridX = 20;
        final int gridY = 20;
        var aStarGrid = buildPathGrid(gridX, gridY);
        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("a* pathfinding (4-way)", () -> new PlayerAStar(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), ASTAR_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "A* Pathfinding",
                                Material.COMPASS,
                                "4-way A* on a fixed 2D obstacle map",
                                "Colors show open, closed, and final path.",
                                "Time: O(E log V) | Space: O(V)"
                        ))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), BFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "BFS Pathfinding",
                                Material.RECOVERY_COMPASS,
                                "4-way BFS explores breadth-first",
                                "Queue-based level-by-level expansion.",
                                "Time: O(V + E) | Space: O(V)"
                        ))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), DFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "DFS Pathfinding",
                                Material.LOOM,
                                "4-way DFS explores depth-first",
                                "Stack-based backtracking expansion.",
                                "Time: O(V + E) | Space: O(V)"
                        ))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), GREEDY_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Greedy Best-First",
                                Material.REDSTONE_TORCH,
                                "Fast heuristic-only pathfinding",
                                "Prioritizes closeness to goal, may miss optimal paths.",
                                "Time: O(E log V) | Space: O(V)"
                        ))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

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

    private static void registerSortingAlgo() {
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
                Algorithm.<Integer, ISceneOps>build(ctx -> ctx
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection1)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort",
                                Material.IRON_SWORD,
                                "A simple sorting algorithm that builds",
                                "the final sorted array one item at a time.",
                                "Time: O(n^2) | Space: O(1)"))
                        .withScene(sceneContext ->
                                new DefaultScene(
                                        sceneContext.instance(),
                                        sceneContext.audience(),
                                        sceneContext.origin()))
                ));


        algo.registerAlgorithm(
                Algorithm.<Integer, ISceneOps>build(ctx -> ctx
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .withData(integerCollection2)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_SMALL_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Small Insertion Sort",
                                Material.GOLDEN_SWORD,
                                "A compact insertion-sort demo",
                                "with fewer values for quick runs.",
                                "Time: O(n^2) | Space: O(1)"))
                        .withScene(sceneContext ->
                                new DefaultScene(
                                        sceneContext.instance(),
                                        sceneContext.audience(),
                                        sceneContext.origin()))
                ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("insertion sort (string)", PlayerInsertion::new)
                        .withData(stringCollection1)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_STRINGS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .onCompletion(_ -> AnimationPlan.empty())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort (Strings)",
                                Material.BOOK,
                                "Insertion-sort using string values",
                                "to demonstrate generic ordering.",
                                "Time: O(n^2) | Space: O(1)"))
                        .withScene(sceneContext ->
                                new DefaultScene(
                                        sceneContext.instance(),
                                        sceneContext.audience(),
                                        sceneContext.origin()))
                ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("sorted insertion", PlayerInsertion::new)
                        .withData(sortedStringCollection)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withScene(sceneContext ->
                                new DefaultScene(
                                        sceneContext.instance(),
                                        sceneContext.audience(),
                                        sceneContext.origin()))
                )
        );
    }

    private static void registerPathFindingAlgo() {
        final int gridX = 20;
        final int gridY = 20;
        var aStarGrid = buildPathGrid(gridX, gridY);

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("a* pathfinding (4-way)", () -> new PlayerAStar(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), MapConstants.ASTAR_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "A* Pathfinding",
                                Material.NETHER_STAR,
                                "4-way A* on a fixed 2D obstacle map",
                                "Colors show open, closed, and final path.",
                                "Time: O(E log V) | Space: O(V)"))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), MapConstants.BFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "BFS Pathfinding",
                                Material.RECOVERY_COMPASS,
                                "4-way BFS explores breadth-first",
                                "Queue-based level-by-level expansion.",
                                "Time: O(V + E) | Space: O(V)"))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), MapConstants.DFS_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "DFS Pathfinding",
                                Material.LOOM,
                                "4-way DFS explores depth-first",
                                "Stack-based backtracking expansion.",
                                "Time: O(V + E) | Space: O(V)"))
                        .withScene(sceneContext -> new GridScene(sceneContext.instance(), sceneContext.audience(), sceneContext.origin()))
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, GridScene>build(ctx -> ctx
                        .withIdentity("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX))
                        .withData(aStarGrid)
                        .positioning(new GridLayout(gridX), MapConstants.GREEDY_2D_PLACEMENT)
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Greedy Best-First",
                                Material.REDSTONE_TORCH,
                                "Fast heuristic-only pathfinding",
                                "Prioritizes closeness to goal, may miss optimal paths.",
                                "Time: O(E log V) | Space: O(V)"))
                        .withScene(sceneContext ->
                                new GridScene(sceneContext.instance(),
                                        sceneContext.audience(), sceneContext.origin()
                                ))
                )
        );
    }
}