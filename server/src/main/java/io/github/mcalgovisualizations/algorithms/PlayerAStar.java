package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PlayerAStar implements IPlayerSort {

    public static final int WALL = 1;
    public static final int START = 2;
    public static final int GOAL = 3;

    private final int columns;

    public PlayerAStar(int columns) {
        if (columns <= 0) throw new IllegalArgumentException("columns must be > 0");
        this.columns = columns;
    }

    @Override
    public <T extends Comparable<T>> void sort(SortingCollection<T> values) {
        int size = values.size();
        if (size == 0) {
            values.emit(new Message("A*: empty grid", Message.MessageType.ERROR));
            return;
        }

        int[] cells = new int[size];
        for (int i = 0; i < size; i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer number)) {
                values.emit(new Message("A*: expected integer grid values", Message.MessageType.ERROR));
                return;
            }
            cells[i] = number;
        }

        int rows = (int) Math.ceil(size / (double) columns);
        int start = -1;
        int goal = -1;

        for (int i = 0; i < size; i++) {
            switch (cells[i]) {
                case WALL -> values.emit(new CellStateTransition(i, CellState.DEFAULT, CellState.WALL));
                case START -> {
                    start = i;
                    values.emit(new CellStateTransition(i, CellState.DEFAULT, CellState.START));
                }
                case GOAL -> {
                    goal = i;
                    values.emit(new CellStateTransition(i, CellState.DEFAULT, CellState.GOAL));
                }
                default -> {
                    // Keep default color for open terrain.
                }
            }
        }

        if (start < 0 || goal < 0) {
            values.emit(new Message("A*: start or goal missing", Message.MessageType.ERROR));
            return;
        }

        int[] g = new int[size];
        int[] parent = new int[size];
        boolean[] closed = new boolean[size];
        boolean[] seenOpen = new boolean[size];
        Arrays.fill(g, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator
                .comparingInt(Node::f)
                .thenComparingInt(Node::h));

        g[start] = 0;
        frontier.add(new Node(start, heuristic(start, goal, columns), heuristic(start, goal, columns)));
        seenOpen[start] = true;

        boolean found = false;
        while (!frontier.isEmpty()) {
            Node currentNode = frontier.poll();
            int current = currentNode.index();

            if (closed[current]) continue;
            closed[current] = true;

            if (current != start && current != goal) {
                values.emit(new CellStateTransition(current, CellState.OPEN, CellState.CLOSED));
            }

            if (current == goal) {
                found = true;
                break;
            }

            int row = current / columns;
            int col = current % columns;

            int[] neighbors = new int[] {
                    index(row - 1, col, rows, columns, size),
                    index(row + 1, col, rows, columns, size),
                    index(row, col - 1, rows, columns, size),
                    index(row, col + 1, rows, columns, size)
            };

            for (int neighbor : neighbors) {
                if (neighbor < 0 || cells[neighbor] == WALL || closed[neighbor]) continue;

                int tentativeG = g[current] + 1;
                if (tentativeG >= g[neighbor]) continue;

                parent[neighbor] = current;
                g[neighbor] = tentativeG;

                int h = heuristic(neighbor, goal, columns);
                frontier.add(new Node(neighbor, tentativeG + h, h));

                if (!seenOpen[neighbor] && neighbor != start && neighbor != goal) {
                    values.emit(new CellStateTransition(neighbor, CellState.DEFAULT, CellState.OPEN));
                }
                seenOpen[neighbor] = true;
            }
        }

        if (!found) {
            values.emit(new Message("A*: no path found", Message.MessageType.ERROR));
            return;
        }

        int pathCursor = goal;
        while (pathCursor != -1) {
            if (pathCursor != start && pathCursor != goal) {
                CellState previous = closed[pathCursor] ? CellState.CLOSED : CellState.OPEN;
                values.emit(new CellStateTransition(pathCursor, previous, CellState.PATH));
            }
            pathCursor = parent[pathCursor];
        }

        values.emit(new Message("A*: path found", Message.MessageType.SUCCESS));
    }

    @Override
    public String getName() {
        return "A* (4-way grid)";
    }

    private static int heuristic(int from, int to, int columns) {
        int fromRow = from / columns;
        int fromCol = from % columns;
        int toRow = to / columns;
        int toCol = to % columns;
        return Math.abs(fromRow - toRow) + Math.abs(fromCol - toCol);
    }

    private static int index(int row, int col, int rows, int columns, int size) {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return -1;
        int idx = (row * columns) + col;
        return idx < size ? idx : -1;
    }

    private record Node(int index, int f, int h) {}
}


