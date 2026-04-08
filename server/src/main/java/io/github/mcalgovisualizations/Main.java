package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.config.MapConstants;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.layouts.*;
import io.github.mcalgovisualizations.ui.GroupedAlgorithmUI;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.visualization.SystemMessages;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.FlowEdgeFlowUpdate;
import io.github.mcalgovisualizations.events.FlowEdgeVisit;
import io.github.mcalgovisualizations.events.FlowPathEdge;
import io.github.mcalgovisualizations.events.FlowStatus;
import io.github.mcalgovisualizations.visualization.algorithms.Message;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
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
        MinecraftServer server = MinecraftServer.init(new Auth.Online());
        InstanceContainer instance = createMainInstance();

        // Sets the game time
        instance.setTimeRate(0);  // Stops time
        instance.setTime(6000);   // Sets time to noon

        algo = new AlgoCraft(instance);
        algo.setSelectorUI(new GroupedAlgorithmUI());
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

        var flowMatrix = new ArrayList<>(Arrays.asList(
                new Data<>(0), new Data<>(16), new Data<>(13), new Data<>(0),  new Data<>(0),  new Data<>(0),
                new Data<>(0), new Data<>(0),  new Data<>(10), new Data<>(12), new Data<>(0),  new Data<>(0),
                new Data<>(0), new Data<>(4),  new Data<>(0),  new Data<>(0),  new Data<>(14), new Data<>(0),
                new Data<>(0), new Data<>(0),  new Data<>(9),  new Data<>(0),  new Data<>(0),  new Data<>(20),
                new Data<>(0), new Data<>(0),  new Data<>(0),  new Data<>(7),  new Data<>(0),  new Data<>(4),
                new Data<>(0), new Data<>(0),  new Data<>(0),  new Data<>(0),  new Data<>(0),  new Data<>(0)
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
                                "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
                        ))
                        .withScene(DefaultScene::new)
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
                                "Time: O(n^2) | Space: O(1)", "with fewer values for quick runs.", "A compact insertion-sort demo"
                        ))
                        .withScene(DefaultScene::new)
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
                                Material.DIAMOND_SWORD,
                                "Time: O(n^2) | Space: O(1)", "to demonstrate generic ordering.", "Insertion-sort using string values"
                        ))
                        .withScene(DefaultScene::new)
        ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("sorted insertion", PlayerInsertion::new)
                        .withData(sortedStringCollection)
                        .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Sorted Insertion",
                                Material.NETHERITE_SWORD,
                                "Best-case: O(n) | Worst-case: O(n^2)",
                                "Already sorted input demo for insertion sort"
                        ))
                        .withScene(DefaultScene::new)
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
                                "Tip: use Randomize before Start to explore new search paths", "then searches for one value using branch decisions.", "Builds a BST from the current values"
                        ))
                        .withScene(DefaultScene::new)
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
                                Material.SPYGLASS,
                                "until the target is found.", "Search must visit nodes in order", "A tree filled level-by-level."
                        ))
                        .withScene(DefaultScene::new)
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
                                "Tip: equal matches follow the middle branch", "then searches for one value using left, middle, and right branches.", "Builds a ternary search tree from the current strings"
                        ))
                        .withScene(DefaultScene::new)
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
                                "Time: O(E log V) | Space: O(V)",
                                "Colors show open, closed, and final path.",
                                "4-way A* on a fixed 2D obstacle map"
                        ))
                        .withScene(GridScene::new)
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
                                Material.COMPASS,
                                "Time: O(V + E) | Space: O(V)",
                                "Queue-based level-by-level expansion.",
                                "4-way BFS explores breadth-first"
                        ))
                        .withScene(GridScene::new)
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
                                Material.COMPASS,
                                "Time: O(V + E) | Space: O(V)",
                                "Stack-based backtracking expansion.",
                                "4-way DFS explores depth-first"
                        ))
                        .withScene(GridScene::new)
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
                                Material.COMPASS,
                                "Time: O(E log V) | Space: O(V)", "Prioritizes closeness to goal, may miss optimal paths.", "Fast heuristic-only pathfinding"
                        ))
                        .withScene(GridScene::new)
                )
        );

        algo.registerAlgorithm(
                Algorithm.<Integer, ISceneOps>build(ctx -> ctx
                        .withIdentity("max flow (edmonds-karp)", PlayerMaxFlow::new)
                        .withData(flowMatrix)
                        .positioning(new GraphNetworkLayout(8.0, 5.0), MAX_FLOW_2D_PLACEMENT)
                        .onEvent(FlowEdgeVisit.class, new FlowEdgeVisitHandler())
                        .onEvent(FlowPathEdge.class, new FlowPathEdgeHandler())
                        .onEvent(FlowEdgeFlowUpdate.class, new FlowEdgeFlowUpdateHandler())
                        .onEvent(FlowStatus.class, new FlowStatusHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Max Flow (Edmonds-Karp)",
                                Material.WATER_BUCKET,
                                "Graph max-flow from source (0) to sink (n-1)",
                                "Fixed 6-node flow graph with edge current/max labels",
                                "Selected augmenting path edges turn particle color"
                        ))
                        .withScene(DefaultScene::new)
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
            PlayerSkin skin = PlayerSkin.fromUsername(player.getUsername());
            if (skin != null) {
                player.setSkin(skin);
            }
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
                                "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
                        ))
                        .withScene(DefaultScene::new)
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
                                "Time: O(n^2) | Space: O(1)", "with fewer values for quick runs.", "A compact insertion-sort demo"
                        ))
                        .withScene(DefaultScene::new)
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
                                "Time: O(n^2) | Space: O(1)", "to demonstrate generic ordering.", "Insertion-sort using string values"
                        ))
                        .withScene(DefaultScene::new)
                ));


        algo.registerAlgorithm(
                Algorithm.<String, ISceneOps>build(ctx -> ctx
                        .withIdentity("sorted insertion", PlayerInsertion::new)
                        .withData(sortedStringCollection)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_INTS_PLACEMENT)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withScene(DefaultScene::new)
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
                                "Time: O(E log V) | Space: O(V)", "Colors show open, closed, and final path.", "4-way A* on a fixed 2D obstacle map"
                        ))
                        .withScene(GridScene::new)
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
                                "Time: O(V + E) | Space: O(V)", "Queue-based level-by-level expansion.", "4-way BFS explores breadth-first"
                        ))
                        .withScene(GridScene::new)
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
                                "Time: O(V + E) | Space: O(V)", "Stack-based backtracking expansion.", "4-way DFS explores depth-first"
                        ))
                        .withScene(GridScene::new)
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
                                "Time: O(E log V) | Space: O(V)", "Prioritizes closeness to goal, may miss optimal paths.", "Fast heuristic-only pathfinding"
                        ))
                        .withScene(GridScene::new)
        ));
    }
}
