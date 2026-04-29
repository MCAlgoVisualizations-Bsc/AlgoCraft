package io.github.mcalgovisualizations.algorithms.GraphSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NodeUtils {
    public static final float GRID_SCALE = 1f;
    public static final int GRID_ROWS = (int) (4 * GRID_SCALE);
    public static final int GRID_COLS = (int) (6 * GRID_SCALE);

    public static Node RandomizeNode() {
        Random random = new Random();
        Node[][] grid = new Node[GRID_ROWS][GRID_COLS];
        List<Node> allNodes = new ArrayList<>();

        // 1. Create nodes and assign them to a grid
        int idCounter = 0;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                Node node = new Node(idCounter++, random.nextInt(90) + 10);
                grid[r][c] = node;
                allNodes.add(node);
            }
        }

        // 2. Connect Orthogonally
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                Node current = grid[r][c];
                if (c + 1 < GRID_COLS && random.nextDouble() < 0.6) {
                    Node right = grid[r][c + 1];
                    current.addNeighbor(right);
                    right.addNeighbor(current);
                }
                if (r + 1 < GRID_ROWS && random.nextDouble() < 0.6) {
                    Node down = grid[r + 1][c];
                    current.addNeighbor(down);
                    down.addNeighbor(current);
                }
            }
        }

        // 3. Guarantee every node has at least one neighbor
        ensureConnectivity(grid);

        // 4. Randomly Assign Start and End
        if (allNodes.size() >= 2) {
            // Pick a random Start node
            Node startNode = allNodes.get(random.nextInt(allNodes.size()));
            startNode.setStatus(Node.NodeTarget.Start);

            // Pick a random End node, ensuring it is not the Start node
            Node endNode;
            do {
                endNode = allNodes.get(random.nextInt(allNodes.size()));
            } while (endNode.equals(startNode));

            endNode.setStatus(Node.NodeTarget.End);

            // Return the startNode so the algorithm knows where to begin
            return startNode;
        }

        return allNodes.isEmpty() ? null : allNodes.getFirst();
    }

    private static void ensureConnectivity(Node[][] grid) {
        for (int r = 0; r < NodeUtils.GRID_ROWS; r++) {
            for (int c = 0; c < NodeUtils.GRID_COLS; c++) {
                if (grid[r][c].getNeighbors().isEmpty()) {
                    if (c + 1 < NodeUtils.GRID_COLS) {
                        grid[r][c].addNeighbor(grid[r][c+1]);
                        grid[r][c+1].addNeighbor(grid[r][c]);
                    } else if (r + 1 < NodeUtils.GRID_ROWS) {
                        grid[r][c].addNeighbor(grid[r+1][c]);
                        grid[r+1][c].addNeighbor(grid[r][c]);
                    }
                }
            }
        }
    }
}
