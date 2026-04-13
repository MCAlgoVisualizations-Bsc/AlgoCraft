package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.PlayerBSTSearch;
import io.github.mcalgovisualizations.algorithms.PlayerInsertion;
import io.github.mcalgovisualizations.algorithms.PlayerTSTSearch;
import io.github.mcalgovisualizations.algorithms.PlayerUnorderedTree;
import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.config.MapConstants;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Message;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.handlers.BstCompareHandler;
import io.github.mcalgovisualizations.handlers.CompareHandler;
import io.github.mcalgovisualizations.handlers.MessageHandler;
import io.github.mcalgovisualizations.handlers.SwapHandler;
import io.github.mcalgovisualizations.layouts.BSTNodeLayout;
import io.github.mcalgovisualizations.layouts.FloatingLinearLayout;
import io.github.mcalgovisualizations.layouts.TSTNodeLayout;
import io.github.mcalgovisualizations.layouts.UnorderedTreeLayout;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.mcalgovisualizations.config.MapConstants.*;

public class RegisterAlgo {
    public static void registerAlgo(AlgoCraft algo) {
        registerSortingAlgo(algo);
        registerTreeSearchAlgo(algo);
        registerPathFindingAlgo(algo);
    }

    private static void registerSortingAlgo(AlgoCraft algo) {
        var integerCollection1 = new SortingContext<>(Arrays.asList(3, 7, 8, 1, 6, 4, 9, 5, 2));
        var stringCollection1 = new SortingContext<>(Arrays.asList("a", "b", "k", "x", "d", "h", "a", "b", "e"));

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection1)
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>(), INSERTION_INTS_PLACEMENT)
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
                Algorithm.builder(integerCollection1)
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>(), MapConstants.INSERTION_INTS_PLACEMENT)
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
                Algorithm.builder(integerCollection1)
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>(), INSERTION_SMALL_PLACEMENT)
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


        algo.registerAlgorithm(
                Algorithm.builder(new SortingContext<>(Arrays.asList(8, 3, 1)))
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>(), MapConstants.INSERTION_SMALL_PLACEMENT)
                        .withPresentation(new AlgorithmPresentation(
                                "Small Insertion Sort",
                                Material.GOLDEN_SWORD,
                                "Time: O(n^2) | Space: O(1)", "with fewer values for quick runs.", "A compact insertion-sort demo"
                        ))
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withScene(DefaultScene::new)
                        .create()
        );


        algo.registerAlgorithm(
            Algorithm.builder(stringCollection1)
                .withIdentity("insertion sort (string)", PlayerInsertion::new)
                .positioning(new FloatingLinearLayout<>(), MapConstants.INSERTION_STRINGS_PLACEMENT)
                .onEvent(Compare.class, new CompareHandler())
                .onEvent(Swap.class, new SwapHandler())
                .onCompletion(_ -> AnimationPlan.empty())
                .withPresentation(new AlgorithmPresentation(
                        "Insertion Sort (Strings)",
                        Material.BOOK,
                        "Time: O(n^2) | Space: O(1)", "to demonstrate generic ordering.", "Insertion-sort using string values"
                ))
                .withScene(DefaultScene::new)
                .create()
        );
    }

    private static void registerTreeSearchAlgo(AlgoCraft algo) {
        var bstCollection = new GridContext<>(Arrays.asList("a", "b", "k", "x", "d", "h", "a", "b", "e", "h", "s", "j", "s", "v", "k"));

        algo.registerAlgorithm(
                Algorithm.builder(bstCollection)
                        .withIdentity("bst search", PlayerBSTSearch::new)
                        //.withData(bstCollection)
                        .positioning(new BSTNodeLayout<>(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Binary Search Tree (Search)",
                                Material.SPYGLASS,
                                "Tip: use Randomize before Start to explore new search paths", "then searches for one value using branch decisions.", "Builds a BST from the current values"
                        ))
                        .withScene(DefaultScene::new)
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(bstCollection)
                        .withIdentity("unordered_tree_search", PlayerUnorderedTree::new)
                        // Use the new Unordered Layout to ensure Root is at index 0 (the top)
                        .positioning(new UnorderedTreeLayout<>(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Unordered Binary Tree (Linear Search)",
                                Material.DARK_OAK_LOG,
                                "until the target is found.", "Search must visit nodes in order", "A tree filled level-by-level."
                        ))
                        .withScene(DefaultScene::new)
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(bstCollection)
                        .withIdentity("tst search", PlayerTSTSearch::new)
                        //.withData(stringCollection1)
                        .positioning(new TSTNodeLayout<>(), BST_SEARCH_PLACEMENT)
                        .onEvent(Compare.class, new BstCompareHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Ternary Search Tree (Search)",
                                Material.SPYGLASS,
                                "Tip: equal matches follow the middle branch", "then searches for one value using left, middle, and right branches.", "Builds a ternary search tree from the current strings"
                        ))
                        .withScene(DefaultScene::new)
                        .create()
        );
    }


    private static void registerPathFindingAlgo(AlgoCraft algo) {
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
}
