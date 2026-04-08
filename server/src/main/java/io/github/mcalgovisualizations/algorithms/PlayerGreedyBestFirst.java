package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.algorithms.Message;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PlayerGreedyBestFirst implements IPlayerSort<SortingContext<Integer>> {

    public static final int WALL = 1;
    public static final int START = 2;
    public static final int GOAL = 3;

    private final int columns;

    public PlayerGreedyBestFirst(int columns) {
        if (columns <= 0) throw new IllegalArgumentException("columns must be > 0");
        this.columns = columns;
    }

    @Override
    public void run(SortingContext<Integer> ctx) {
        var values = ctx.getData();
        int size = values.size();
        if (size == 0) {
            ctx.emit(new Message("Greedy Best-First: empty grid", Message.MessageType.ERROR));
            return;
        }

        int rows = (int) Math.ceil(size / (double) columns);
        int start = -1;
        int goal = -1;

        for (int i = 0; i < size; i++) {
            if (values.get(i) == START) start = i;
            if (values.get(i) == GOAL) goal = i;
        }

        if (start < 0 || goal < 0) {
            ctx.emit(new Message("Greedy Best-First: start or goal missing", Message.MessageType.ERROR));
            return;
        }

        boolean[] visited = new boolean[size];
        int[] parent = new int[size];
        Arrays.fill(parent, -1);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(Node::h));

        frontier.add(new Node(start, heuristic(start, goal, columns)));
        visited[start] = true;

        boolean found = false;
        while (!frontier.isEmpty()) {
            Node currentNode = frontier.poll();
            int current = currentNode.index();

            if (current == goal) {
                found = true;
                break;
            }

            if (current != start) {
                ctx.emit(new CellStateTransition(current, CellState.OPEN, CellState.CLOSED));
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
                if (neighbor < 0 || values.get(neighbor) == WALL || visited[neighbor]) continue;

                parent[neighbor] = current;
                frontier.add(new Node(neighbor, heuristic(neighbor, goal, columns)));
                visited[neighbor] = true;

                if (neighbor != goal) {
                    ctx.emit(new CellStateTransition(neighbor, CellState.DEFAULT, CellState.OPEN));
                }
            }
        }

        if (!found) {
            ctx.emit(new Message("Greedy Best-First: no path found", Message.MessageType.ERROR));
            return;
        }

        int pathCursor = goal;
        while (pathCursor != -1) {
            if (pathCursor != start && pathCursor != goal) {
                ctx.emit(new CellStateTransition(pathCursor, CellState.CLOSED, CellState.PATH));
            }
            pathCursor = parent[pathCursor];
        }

        ctx.emit(new Message("Greedy Best-First: path found", Message.MessageType.SUCCESS));
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

    private record Node(int index, int h) {}
}

