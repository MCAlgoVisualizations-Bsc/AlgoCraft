package io.github.mcalgovisualizations.config;

import io.github.mcalgovisualizations.visualization.AlgorithmPlacement;
import net.minestom.server.coordinate.Pos;

public final class MapConstants {
    public static final Pos HUB_SPAWN = new Pos(194, 137, -38);

    public static final AlgorithmPlacement INSERTION_INTS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));
    public static final AlgorithmPlacement INSERTION_SMALL_PLACEMENT =
            new AlgorithmPlacement(new Pos(193, 138, 132), new Pos(194.5, 139, 136));
    public static final AlgorithmPlacement INSERTION_STRINGS_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 132), new Pos(194.5, 139, 136));
    public static final AlgorithmPlacement ASTAR_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 200, 145), new Pos(194.5, 200, 148));
    public static final AlgorithmPlacement BFS_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(200, 200, 145), new Pos(207.5, 200, 148));
    public static final AlgorithmPlacement DFS_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(213, 200, 145), new Pos(220.5, 200, 148));
    public static final AlgorithmPlacement GREEDY_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(226, 200, 145), new Pos(233.5, 200, 148));
    public static final AlgorithmPlacement MAX_FLOW_2D_PLACEMENT =
            new AlgorithmPlacement(new Pos(239, 200, 145), new Pos(246.5, 200, 148));
    public static final AlgorithmPlacement BST_SEARCH_PLACEMENT =
            new AlgorithmPlacement(new Pos(187, 138, 120), new Pos(194.5, 139, 136));
}
