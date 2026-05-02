package io.github.mcalgovisualizations.algorithms.mazes;

import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.events.Message;

import java.util.Arrays;
import java.util.Stack;

public class PlayerDFS implements IPlayerSort<GridContext<Integer>> {

    public static final int WALL = 1;
    public static final int START = 2;
    public static final int GOAL = 3;

    private final int columns;

    public PlayerDFS(int columns) {
        if (columns <= 0) throw new IllegalArgumentException("columns must be > 0");
        this.columns = columns;
    }

    @Override
    public void run(GridContext<Integer> ctx) {
        var values = ctx.values;
        int size = values.size();
        if (size == 0) {
            ctx.emit(new Message("DFS: empty grid", Message.MessageType.ERROR));
            return;
        }

        int[] cells = new int[size];
        for (int i = 0; i < size; i++) {
            var number = values.get(i);

            cells[i] = number;
        }

        int rows = (int) Math.ceil(size / (double) columns);
        int start = -1;
        int goal = -1;

        for (int i = 0; i < size; i++) {
            if (cells[i] == START) start = i;
            if (cells[i] == GOAL) goal = i;
        }

        if (start < 0 || goal < 0) {
            ctx.emit(new Message("DFS: start or goal missing", Message.MessageType.ERROR));
            return;
        }

        boolean[] visited = new boolean[size];
        int[] parent = new int[size];
        Arrays.fill(parent, -1);

        Stack<Integer> frontier = new Stack<>();
        frontier.push(start);

        boolean found = false;
        while (!frontier.isEmpty()) {
            int current = frontier.pop();

            if (visited[current]) continue;
            visited[current] = true;

            if (current == goal) {
                found = true;
                break;
            }

            if (current != start && current != goal) {
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
                if (neighbor < 0 || cells[neighbor] == WALL || visited[neighbor]) continue;

                parent[neighbor] = current;
                frontier.push(neighbor);

                if (neighbor != start && neighbor != goal) {
                    ctx.emit(new CellStateTransition(neighbor, CellState.DEFAULT, CellState.OPEN));
                }
            }
        }

        if (!found) {
            ctx.emit(new Message("DFS: no path found", Message.MessageType.ERROR));
            return;
        }

        int pathCursor = goal;
        while (pathCursor != -1) {
            if (pathCursor != start && pathCursor != goal) {
                CellState previous = visited[pathCursor] ? CellState.CLOSED : CellState.OPEN;
                ctx.emit(new CellStateTransition(pathCursor, previous, CellState.PATH));
            }
            pathCursor = parent[pathCursor];
        }

        ctx.emit(new Message("DFS: path found", Message.MessageType.SUCCESS));
    }

    private static int index(int row, int col, int rows, int columns, int size) {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return -1;
        int idx = (row * columns) + col;
        return idx < size ? idx : -1;
    }
}

