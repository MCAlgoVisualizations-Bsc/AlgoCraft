package io.github.mcalgovisualizations.layouts;

public final class FlowNetworkRules {
    public static final int NODE_COUNT = 6;
    public static final int MATRIX_SIZE = NODE_COUNT * NODE_COUNT;

    // Directed adjacency constraints for A..F flow visualizations.
    private static final boolean[][] ALLOWED = {
            // A      B      C      D      E      F
            /* A */ {false, true,  true,  false, false, false}, // A (Source) sends to B, C
            /* B */ {false, false, true,  true,  true,  false}, // B sends to D, E (and C cross-lane)
            /* C */ {false, false, false, true,  true,  false}, // C sends to D, E
            /* D */ {false, false, false, false, true,  true }, // D sends to F (and E cross-lane)
            /* E */ {false, false, false, false, false, true }, // E sends to F
            /* F */ {false, false, false, false, false, false}  // F (Sink) sends nowhere!
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

