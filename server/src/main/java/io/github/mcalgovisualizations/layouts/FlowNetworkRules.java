package io.github.mcalgovisualizations.layouts;

public final class FlowNetworkRules {
    public static final int NODE_COUNT = 6;
    public static final int MATRIX_SIZE = NODE_COUNT * NODE_COUNT;
    // Guaranteed source->sink backbones.
    public static final int[] GUARANTEED_TOP_SPINE_NODE_PATH = {0, 1, 3, 5};
    public static final int[] GUARANTEED_BOTTOM_SPINE_NODE_PATH = {0, 2, 4, 5};
    public static final int GUARANTEED_SPINE_MIN_CAPACITY = 5;

    // Directed adjacency constraints for A..F flow visualizations.
    private static final boolean[][] ALLOWED = {
            //         A      B      C      D      E      F
            /* A */ {false, true,  true,  false, false, false}, // A (Source) sends to B, C
            /* B */ {false, false, true,  true,  true,  false}, // B sends to D, E (and C cross-lane)
            /* C */ {false, true,  false, true,  true,  false}, // C sends to D, E
            /* D */ {false, false, false, false, true,  true }, // D sends to F (and E cross-lane)
            /* E */ {false, false, false, false, false, true }, // E sends to F
            /* F */ {false, false, false, false, false, false}  // F (Sink) sends nowhere!
    };


    public static boolean isAllowedDirectedEdge(int from, int to) {
        return from >= 0 && to >= 0 && from < NODE_COUNT && to < NODE_COUNT && ALLOWED[from][to];
    }

    public static boolean isGuaranteedSpineEdge(int from, int to) {
        return isEdgeInPath(from, to, GUARANTEED_TOP_SPINE_NODE_PATH)
                || isEdgeInPath(from, to, GUARANTEED_BOTTOM_SPINE_NODE_PATH);
    }

    private static boolean isEdgeInPath(int from, int to, int[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            if (from == path[i] && to == path[i + 1]) {
                return true;
            }
        }
        return false;
    }

    public static int effectiveCapacity(int from, int to, int rawCapacity) {
        if (!isAllowedDirectedEdge(from, to)) {
            return 0;
        }

        int normalized = Math.max(0, rawCapacity);
        if (isGuaranteedSpineEdge(from, to) && normalized == 0) {
            return GUARANTEED_SPINE_MIN_CAPACITY;
        }
        return normalized;
    }

    // Fallback used by algorithm helpers for non-6x6 matrices.
    public static boolean isAllowedDirectedEdge(int from, int to, int nodeCount) {
        if (nodeCount == NODE_COUNT) {
            return isAllowedDirectedEdge(from, to);
        }
        return from >= 0 && to >= 0 && from < nodeCount && to < nodeCount && from != to;
    }
}

