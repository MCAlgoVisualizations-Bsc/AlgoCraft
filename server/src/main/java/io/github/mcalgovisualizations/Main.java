package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.config.MapConstants;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.layouts.*;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Message;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.ContextFactory;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.*;
import java.util.function.Consumer;

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

        registerSortingAlgo();
        registerTreeSearchAlgo();
        registerPathFindingAlgo();

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
            player.sendMessage(Component.text(
                    "Right-click the Nether Star to select an algorithm to visualize!", NamedTextColor.YELLOW));
            player.sendMessage(Component.text(
                    "Welcome to Algorithm Visualizations!", Message.MessageType.SUCCESS.color()));

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

    private static ArrayList<Integer> buildPathGrid(int xSize, int ySize) {
        if (xSize <= 0 || ySize <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be > 0");
        }

        ArrayList<Integer> grid = new ArrayList<>(xSize * ySize);
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
                grid.add(value);
            }
        }
        return grid;
    }

    private static void registerSortingAlgo() {
        var integerCollection1 = new SortingContext<>(new ArrayList<>(Arrays.asList(3, 7, 8, 1, 6, 4, 9, 5, 2)));
        var integerCollection2 =new SortingContext<Integer>(new ArrayList<>(Arrays.asList(8, 3, 1)));
        var sortedStringCollection = new SortingContext<String>(new ArrayList<>(Arrays.asList("a", "b", "c")));
        var stringCollection1 = new ArrayList<>(Arrays.asList("a", "b", "k","x", "d", "h", "a", "b", "e"));

        algo.registerAlgorithm(
            Algorithm.builder(integerCollection2)
            .withIdentity("insertion sort (ints)", PlayerInsertion::new)
            .positioning(new FloatingLinearLayout(), INSERTION_INTS_PLACEMENT)
            .withScene(DefaultScene::new)
            .onEvent(Compare.class, new CompareHandler())
            .onEvent(Swap.class, new SwapHandler())
            .withPresentation(new AlgorithmPresentation(
                    "Insertion Sort",
                    Material.IRON_SWORD,
                    "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
            ))
            .withContextFactory(SortingContext::new, ArrayList::new, lst -> {
                Collections.shuffle(lst);
                return lst;
            })
            .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection2)
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_INTS_PLACEMENT)
                        .withScene(DefaultScene::new)
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort",
                                Material.IRON_SWORD,
                                "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
                        ))
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withContextFactory(SortingContext::new, ArrayList::new, lst -> {
                            Collections.shuffle(lst);
                            return lst;
                        })
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection2)
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout(), INSERTION_SMALL_PLACEMENT)
                        .withScene(DefaultScene::new)
                        .withPresentation(new AlgorithmPresentation(
                                "Small insertion sort (ints)",
                                Material.GOLDEN_SWORD,
                                "Time: O(n^2) | Space: O(1)",
                                "with fewer values for quick runs.",
                                "A compact insertion-sort demo"
                        ))
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withContextFactory(SortingContext::new, ArrayList::new, lst -> {
                            Collections.shuffle(lst);
                            return lst;
                        })
                        .create()

        );

        /**
         * Algorithm.build(integerCollection2, ctx -> ctx
         *                         .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
         *                         .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_SMALL_PLACEMENT)
         *                         .onEvent(Compare.class, new CompareHandler())
         *                         .onEvent(Swap.class, new SwapHandler())
         *                         .withPresentation(new AlgorithmPresentation(
         *                                 "Small Insertion Sort",
         *                                 Material.GOLDEN_SWORD,
         *                                 "Time: O(n^2) | Space: O(1)", "with fewer values for quick runs.", "A compact insertion-sort demo"
         *                         ))
         *                         .withScene(DefaultScene::new)
         *                 )
         */

//        algo.registerAlgorithm(
//                Algorithm.build(stringCollection1, ctx -> ctx
//                        .withIdentity("insertion sort (string)", PlayerInsertion::new)
//                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_STRINGS_PLACEMENT)
//                        .onEvent(Compare.class, new CompareHandler())
//                        .onEvent(Swap.class, new SwapHandler())
//                        .onCompletion(_ -> AnimationPlan.empty())
//                        .withPresentation(new AlgorithmPresentation(
//                                "Insertion Sort (Strings)",
//                                Material.BOOK,
//                                "Time: O(n^2) | Space: O(1)", "to demonstrate generic ordering.", "Insertion-sort using string values"
//                        ))
//                        .withScene(DefaultScene::new)
//                ));
//
//
//        algo.registerAlgorithm(
//                Algorithm.build(sortedStringCollection, ctx -> ctx
//                        .withIdentity("sorted insertion", PlayerInsertion::new)
//                        .positioning(new FloatingLinearLayout(), MapConstants.INSERTION_INTS_PLACEMENT)
//                        .onEvent(Compare.class, new CompareHandler())
//                        .onEvent(Swap.class, new SwapHandler())
//                        .withScene(DefaultScene::new)
//                )
//        );
    }

    private static void registerPathFindingAlgo() {
//        final int gridX = 20;
//        final int gridY = 20;
//        var aStarGrid = buildPathGrid(gridX, gridY);
//        algo.registerAlgorithm(
//                Algorithm.build(aStarGrid, ctx -> ctx
//                        .withIdentity("a* pathfinding (4-way)", () -> new PlayerAStar(gridX))
//                        .positioning(new GridLayout(gridX), ASTAR_2D_PLACEMENT)
//                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
//                        .onEvent(Message.class, new MessageHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "A* Pathfinding",
//                                Material.COMPASS,
//                                "Time: O(E log V) | Space: O(V)",
//                                "Colors show open, closed, and final path.",
//                                "4-way A* on a fixed 2D obstacle map"
//                        ))
//                        .withScene(GridScene::new)
//                        .withContextFactory(
//                                s -> new GridContext(s)
//                        )
//                )
//        );
//
//        algo.registerAlgorithm(
//                Algorithm.build(null, ctx -> ctx
//                        .withIdentity("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX))
//                        .withData(aStarGrid)
//                        .positioning(new GridLayout(gridX), BFS_2D_PLACEMENT)
//                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
//                        .onEvent(Message.class, new MessageHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "BFS Pathfinding",
//                                Material.RECOVERY_COMPASS,
//                                "Time: O(V + E) | Space: O(V)",
//                                "Queue-based level-by-level expansion.",
//                                "4-way BFS explores breadth-first"
//                        ))
//                        .withScene(GridScene::new)
//                )
//        );
//
//        algo.registerAlgorithm(
//                Algorithm.build(ctx -> ctx
//                        .withIdentity("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX))
//                        .withData(aStarGrid)
//                        .positioning(new GridLayout(gridX), DFS_2D_PLACEMENT)
//                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
//                        .onEvent(Message.class, new MessageHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "DFS Pathfinding",
//                                Material.LOOM,
//                                "Time: O(V + E) | Space: O(V)",
//                                "Stack-based backtracking expansion.",
//                                "4-way DFS explores depth-first"
//                        ))
//                        .withScene(GridScene::new)
//                )
//        );
//
//        algo.registerAlgorithm(
//                Algorithm.build(ctx -> ctx
//                        .withIdentity("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX))
//                        .withData(aStarGrid)
//                        .positioning(new GridLayout(gridX), GREEDY_2D_PLACEMENT)
//                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
//                        .onEvent(Message.class, new MessageHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "Greedy Best-First",
//                                Material.REDSTONE_TORCH,
//                                "Time: O(E log V) | Space: O(V)", "Prioritizes closeness to goal, may miss optimal paths.", "Fast heuristic-only pathfinding"
//                        ))
//                        .withScene(GridScene::new)
//                )
//        );
    }

    private static void registerTreeSearchAlgo() {


//        algo.registerAlgorithm(
//                Algorithm.build(ctx -> ctx
//                        .withIdentity("bst search", PlayerBSTSearch::new)
//                        //.withData(bstCollection)
//                        .positioning(new BSTNodeLayout(), BST_SEARCH_PLACEMENT)
//                        .onEvent(Compare.class, new BstCompareHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "Binary Search Tree (Search)",
//                                Material.SPYGLASS,
//                                "Tip: use Randomize before Start to explore new search paths", "then searches for one value using branch decisions.", "Builds a BST from the current values"
//                        ))
//                        .withScene(DefaultScene::new)
//                )
//        );
//
//        //var unordered_tree_search_data = new ArrayList<>(bstCollection);
//        //Collections.shuffle(unordered_tree_search_data);
//
//
//        algo.registerAlgorithm(
//                Algorithm.build(ctx -> ctx
//                        .withIdentity("unordered_tree_search", PlayerUnorderedTree::new)
//                        //.withData(unordered_tree_search_data)
//                        // Use the new Unordered Layout to ensure Root is at index 0 (the top)
//                        .positioning(new UnorderedTreeLayout(), BST_SEARCH_PLACEMENT)
//                        .onEvent(Compare.class, new BstCompareHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "Unordered Binary Tree (Linear Search)",
//                                Material.DARK_OAK_LOG,
//                                "until the target is found.", "Search must visit nodes in order", "A tree filled level-by-level."
//                        ))
//                        .withScene(DefaultScene::new)
//                )
//        );
//
//        algo.registerAlgorithm(
//                Algorithm.build(ctx -> ctx
//                        .withIdentity("tst search", PlayerTSTSearch::new)
//                        //.withData(stringCollection1)
//                        .positioning(new TSTNodeLayout(), BST_SEARCH_PLACEMENT)
//                        .onEvent(Compare.class, new BstCompareHandler())
//                        .onEvent(Message.class, new MessageHandler())
//                        .withPresentation(new AlgorithmPresentation(
//                                "Ternary Search Tree (Search)",
//                                Material.SPYGLASS,
//                                "Tip: equal matches follow the middle branch", "then searches for one value using left, middle, and right branches.", "Builds a ternary search tree from the current strings"
//                        ))
//                        .withScene(DefaultScene::new)
//                )
//        );
    }
}