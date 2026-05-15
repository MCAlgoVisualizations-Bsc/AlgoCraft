package io.github.mcalgovisualizations.algorithms.GraphSearch;

import java.util.*;

public class NodeUtils {
    public static final float GRID_SCALE = .75f;
    public static final int GRID_ROWS = (int) (4 * GRID_SCALE);
    public static final int GRID_COLS = (int) (6 * GRID_SCALE);

    public static Node test() {
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

    public static Node RandomizeNode() {
        Random random = new Random();
        Map<String, Node> grid = new HashMap<>();
        List<Node> existingNodes = new ArrayList<>();

        // Starting point
        Node startNode = new Node(0, 0, Node.NodeTarget.Start);
        grid.put("0:0", startNode);
        existingNodes.add(startNode);

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int i = 1; i < 8; i++) {
            boolean placed = false;
            while (!placed) {
                // Pick a random node we've already placed to branch off from
                Node parent = existingNodes.get(random.nextInt(existingNodes.size()));

                // This is a bit of a hack: we'd need to store coordinates in the Node
                // or a wrapper to know where the 'parent' is.
                // Let's assume we find its coords from the map:
                String parentKey = grid.entrySet().stream()
                        .filter(e -> e.getValue().equals(parent))
                        .map(java.util.Map.Entry::getKey)
                        .findFirst().get();

                String[] parts = parentKey.split(":");
                int nextX = Integer.parseInt(parts[0]);
                int nextZ = Integer.parseInt(parts[1]);

                int[] dir = directions[random.nextInt(4)];
                nextX += dir[0];
                nextZ += dir[1];

                String newKey = nextX + ":" + nextZ;

                if (!grid.containsKey(newKey)) {
                    Node newNode = new Node(i, i, Node.NodeTarget.None);
                    grid.put(newKey, newNode);
                    existingNodes.add(newNode);
                    connectUndirected(parent, newNode);
                    placed = true;
                }
            }
        }

        // Set a random node (other than start) as the end
        existingNodes.getLast().setStatus(Node.NodeTarget.End);

        return startNode;
    }

    private static void connectUndirected(Node a, Node b) {
        a.addNeighbor(b);
        b.addNeighbor(a);
    }
}
