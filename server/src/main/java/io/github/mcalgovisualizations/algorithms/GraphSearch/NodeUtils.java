package io.github.mcalgovisualizations.algorithms.GraphSearch;

public class NodeUtils {
    public static final float GRID_SCALE = .75f;
    public static final int GRID_ROWS = (int) (4 * GRID_SCALE);
    public static final int GRID_COLS = (int) (6 * GRID_SCALE);

    public static Node RandomizeNode() {
        Node one = new Node(0, 1, Node.NodeTarget.Start);
        Node two = new Node(1, 2, Node.NodeTarget.None);
        Node three = new Node(2, 3, Node.NodeTarget.None);
        Node four = new Node(4, 4, Node.NodeTarget.None);
        Node five = new Node(5, 5, Node.NodeTarget.None);
        Node six = new Node(6, 6, Node.NodeTarget.None);
        Node seven = new Node(7, 7, Node.NodeTarget.None);
        Node eight = new Node(8, 8, Node.NodeTarget.End);

        // Graph: 1-2, 2-3, 3-1, 1-4
        connectUndirected(one, two);
        connectUndirected(two, three);
        connectUndirected(three, four);
        connectUndirected(one, four);
        connectUndirected(five, four);
        connectUndirected(five, six);
        connectUndirected(six, seven);
        connectUndirected(seven, eight);


        return one;
    }

    private static void connectUndirected(Node a, Node b) {
        a.addNeighbor(b);
        b.addNeighbor(a);
    }
}
