package io.github.mcalgovisualizations.DataTypes;

import io.github.mcalgovisualizations.algorithms.GraphSearch.Node;

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
                // value is a random number, id is unique
                Node node = new Node(idCounter++, random.nextInt(90) + 10);
                grid[r][c] = node;
                allNodes.add(node);
            }
        }

        // 2. Connect only Orthogonally (No j + 1 skips!)
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                Node current = grid[r][c];

                // Potential Horizontal connection (Right)
                if (c + 1 < GRID_COLS && random.nextDouble() < 0.6) {
                    Node right = grid[r][c + 1];
                    current.addNeighbor(right);
                    right.addNeighbor(current);
                }

                // Potential Vertical connection (Down)
                if (r + 1 < GRID_ROWS && random.nextDouble() < 0.6) {
                    Node down = grid[r + 1][c];
                    current.addNeighbor(down);
                    down.addNeighbor(current);
                }
            }
        }

        // 3. Guarantee a path exists (No isolated islands)
        // Connect every node to at least one neighbor to ensure it's a single graph
        ensureConnectivity(grid);

        return allNodes.getFirst();
    }

    private static void ensureConnectivity(Node[][] grid) {
        for (int r = 0; r < NodeUtils.GRID_ROWS; r++) {
            for (int c = 0; c < NodeUtils.GRID_COLS; c++) {
                if (grid[r][c].neighbors().isEmpty()) {
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
