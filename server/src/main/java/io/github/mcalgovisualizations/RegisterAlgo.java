package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.algorithms.PlayerAStar;
import io.github.mcalgovisualizations.algorithms.sort.ArcLayout;
import io.github.mcalgovisualizations.algorithms.sort.insertion.InsertionScene;
import io.github.mcalgovisualizations.algorithms.sort.insertion.PlayerInsertion;
import io.github.mcalgovisualizations.algorithms.sort.selection.PlayerSelectionSort;
import io.github.mcalgovisualizations.algorithms.sort.selection.SelectionScene;
import io.github.mcalgovisualizations.events.*;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.layouts.*;
import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.visualization.instance.Algorithm;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterAlgo {
    public static void registerAlgo(AlgoCraft algo) {
        showcaseAlgo(algo);
        //registerTreeSearchAlgo(algo);
        //registerPathFindingAlgo(algo);
        //registerFlowAlgo(algo);
    }

    private static void showcaseAlgo(AlgoCraft algo) {
        // Sorting
        var circleCollection1 = new SortingContext<>(new ArrayList<>(List.of(6,5,4,8,10,9,19,20,2)));

        algo.registerAlgorithm(
                Algorithm.builder(circleCollection1)
                        .withIdentity("Selection sort", PlayerSelectionSort::new)
                        .positioning(new ArcLayout())
                        .withScene(SelectionScene::new)
                        .onEvent(Compare.class, new PlayerSelectionSort.CompareHandler())
                        .onEvent(Swap.class, new PlayerSelectionSort.SwapHandler())
                        .onEvent(PlayerSelectionSort.TrackI.class, new PlayerSelectionSort.TrackIHandler())
                        .onEvent(PlayerSelectionSort.TrackMinIndex.class, new PlayerSelectionSort.TrackMinIndexHandler())
                        .onCompletion(ctx -> {
                            final var size = ctx.getData().size();
                            final var plan = AnimationPlan.<SelectionScene>builder();
                            final var message = Component.text("Algorithm is complete! Final sorted array: " + ctx.getData(), NamedTextColor.YELLOW);
                            plan.step(scene -> scene.sendMessage(message));
                            for(int i = 0; i < size; i++) {
                                final var finalI = i;
                                plan.step(scene -> scene.hoverDisplay(finalI, true));
                            }

                            return plan.build();
                        })
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(circleCollection1)
                        .withIdentity("Circle Layout insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new ArcLayout())
                        .withScene(InsertionScene::new)
                        .onEvent(Compare.class, new PlayerInsertion.CompareHandler())
                        .onEvent(Swap.class, new PlayerInsertion.SwapHandler())
                        .onEvent(PlayerInsertion.TrackI.class, new PlayerInsertion.TrackIHandler())
                        .onEvent(PlayerInsertion.TrackJ.class, new PlayerInsertion.TrackJHandler())
                        .onEvent(PlayerInsertion.Inserted.class, new PlayerInsertion.InsertedHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion sort circular sorting",
                                Material.GOLDEN_APPLE,
                                "Time: O(n^2) | Space: O(1)"
                        ))
                        .onCompletion(ctx -> {
                            final int size = ctx.values.size();
                            var plan = AnimationPlan.<InsertionScene>builder()
                                    .step(InsertionScene::clearGlowing)
                                    .step(circleScene -> circleScene.sendMessage(Component.text(
                                            "Final sorted array: " + ctx.values.toString(), NamedTextColor.GREEN)));

                            for(int i = 0; i<size; i++) {
                                int finalI = i;
                                plan.step(0, circleScene -> circleScene.hoverDisplay(finalI, true));
                            }
                            for(int i = 0; i<size; i++) {
                                int finalI = i;
                                plan.step(0, circleScene -> circleScene.hoverDisplay(finalI, false));
                            }

                            return plan.build();
                        })
                        .create()
        );
    }

    private static void registerFlowAlgo(AlgoCraft algo) {
        var flowMatrix = new GridContext<Integer>(new ArrayList<>(Arrays.asList(
                0,16, 13, 0,  0,  0,
                0,0,  10, 12, 0,  0,
                0,4,  0,  0,  14, 0,
                0,0,  9,  0,  0,  20,
                0,0,  0,  7,  0,  4,
                0,0,  0,  0,  0,  0
        )));
            var e = Algorithm.builder(flowMatrix)
                .withIdentity("max flow (edmonds-karp)", PlayerMaxFlow::new)
                .positioning(new GraphNetworkLayout<>(8.0, 5.0))
                .withScene(FlowScene::new)
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
                .create();
        algo.registerAlgorithm(e);
    }

    private static void registerSortingAlgo(AlgoCraft algo) {
        var integerCollection1 = new SortingContext<>(new ArrayList<>(List.of(3, 7, 8, 1, 6, 4, 9, 5, 2)));
        var stringCollection1 = new SortingContext<>(new ArrayList<>(List.of("a", "b", "k", "x", "d", "h", "a", "b", "e")));

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection1)
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>())
                        .withScene(DefaultScene::new)
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort",
                                Material.IRON_SWORD,
                                "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
                        ))
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection1)
                        .withIdentity("insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>())
                        .withScene(DefaultScene::new)
                        .withPresentation(new AlgorithmPresentation(
                                "Insertion Sort",
                                Material.IRON_SWORD,
                                "Time: O(n^2) | Space: O(1)", "the final sorted array one item at a time.", "A simple sorting algorithm that builds"
                        ))
                        .onEvent(Compare.class, new CompareHandler())
                        .onEvent(Swap.class, new SwapHandler())
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(integerCollection1)
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>())
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
                        .create()

        );


        algo.registerAlgorithm(
                Algorithm.builder(new SortingContext<>(Arrays.asList(8, 3, 1)))
                        .withIdentity("small insertion sort (ints)", PlayerInsertion::new)
                        .positioning(new FloatingLinearLayout<>())
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
                .positioning(new FloatingLinearLayout<>())
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
                        .positioning(new BSTNodeLayout<>())
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
                        .positioning(new UnorderedTreeLayout<>())
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
                        .positioning(new TSTNodeLayout<>())
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
        final int gridX = 20;
        final int gridY = 20;
        var aStarGrid = buildPathGrid(gridX, gridY);
        algo.registerAlgorithm(
                Algorithm.builder(aStarGrid)
                        .withIdentity("a* pathfinding (4-way)", () -> new PlayerAStar(gridX))
                        .positioning(new GridLayout(gridX))
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
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(aStarGrid)
                        .withIdentity("bfs pathfinding (4-way)", () -> new PlayerBFS(gridX))
                        .positioning(new GridLayout(gridX))
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "BFS Pathfinding",
                                Material.RECOVERY_COMPASS,
                                "Time: O(V + E) | Space: O(V)",
                                "Queue-based level-by-level expansion.",
                                "4-way BFS explores breadth-first"
                        ))
                        .withScene(GridScene::new)
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(aStarGrid)
                        .withIdentity("dfs pathfinding (4-way)", () -> new PlayerDFS(gridX))
                        .positioning(new GridLayout(gridX))
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "DFS Pathfinding",
                                Material.LOOM,
                                "Time: O(V + E) | Space: O(V)",
                                "Stack-based backtracking expansion.",
                                "4-way DFS explores depth-first"
                        ))
                        .withScene(GridScene::new)
                        .create()
        );

        algo.registerAlgorithm(
                Algorithm.builder(aStarGrid)
                        .withIdentity("greedy best-first (4-way)", () -> new PlayerGreedyBestFirst(gridX))
                        .positioning(new GridLayout(gridX))
                        .onEvent(CellStateTransition.class, new CellStateTransitionHandler())
                        .onEvent(Message.class, new MessageHandler())
                        .withPresentation(new AlgorithmPresentation(
                                "Greedy Best-First",
                                Material.REDSTONE_TORCH,
                                "Time: O(E log V) | Space: O(V)", "Prioritizes closeness to goal, may miss optimal paths.", "Fast heuristic-only pathfinding"
                        ))
                        .withScene(GridScene::new)
                        .create()
        );
    }

    private static GridContext<Integer> buildPathGrid(int xSize, int ySize) {
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

        return new GridContext<>(grid);
    }
}
