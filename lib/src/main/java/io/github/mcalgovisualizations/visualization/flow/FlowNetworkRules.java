package io.github.mcalgovisualizations.visualization.flow;

public final class FlowNetworkRules {
    public static final int NODE_COUNT = 6;
    public static final int MATRIX_SIZE = NODE_COUNT * NODE_COUNT;

    // Directed adjacency constraints for A..F flow visualizations.
    private static final boolean[][] ALLOWED = {
            // A      B      C      D      E      F
            /* A */ {false, true,  true,  false, false, false},
            /* B */ {false, false, true,  true,  true,  false},
            /* C */ {false, false, false, true,  true,  false},
            /* D */ {false, false, false, false, true,  true },
            /* E */ {false, false, false, false, false, true },
            /* F */ {false, false, false, false, false, false}
    };

    private FlowNetworkRules() {
    }

    public static boolean isAllowedDirectedEdge(int from, int to) {
        return from >= 0 && to >= 0 && from < NODE_COUNT && to < NODE_COUNT && ALLOWED[from][to];
    }

    // Fallback used by algorithm helpers for non-6x6 matrices.
    public static boolean isAllowedDirectedEdge(int from, int to, int nodeCount) {
        if (nodeCount == NODE_COUNT) {
            return isAllowedDirectedEdge(from, to);
        }
        return from >= 0 && to >= 0 && from < nodeCount && to < nodeCount && from != to;
    }
}

