package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.events.CaveVillagerMove;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.events.Message;
import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class PlayerAStar3DTunnel implements IPlayerSort<GridContext<Integer>> {
    private static final int WALL = 1;
    private static final int START = 2;
    private static final int GOAL = 3;

    private final int columns;
    private final int layers;
    private final int depth;

    public PlayerAStar3DTunnel(int columns, int layers, int depth) {
        if (columns <= 0 || layers <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Cave dimensions must be > 0");
        }
        this.columns = columns;
        this.layers = layers;
        this.depth = depth;
    }

    @Override
    public void run(GridContext<Integer> ctx) {
        var values = ctx.getData();
        int size = values.size();
        if (size == 0) {
            ctx.emit(new Message("A* cave: empty grid", Message.MessageType.ERROR));
            return;
        }

        int[] cells = new int[size];
        for (int i = 0; i < size; i++) {
            cells[i] = values.get(i);
        }

        int start = -1;
        int goal = -1;
        for (int i = 0; i < size; i++) {
            if (cells[i] == START) start = i;
            if (cells[i] == GOAL) goal = i;
        }

        if (start < 0 || goal < 0) {
            ctx.emit(new Message("A* cave: start or goal missing", Message.MessageType.ERROR));
            return;
        }

        int[] g = new int[size];
        int[] parent = new int[size];
        boolean[] closed = new boolean[size];
        boolean[] seenOpen = new boolean[size];
        Arrays.fill(g, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(Node::f).thenComparingInt(Node::h));
        g[start] = 0;
        frontier.add(new Node(start, heuristic(start, goal), heuristic(start, goal)));
        seenOpen[start] = true;

        boolean found = false;
        while (!frontier.isEmpty()) {
            Node currentNode = frontier.poll();
            int current = currentNode.index();
            if (closed[current]) continue;
            closed[current] = true;

            if (current != start && current != goal) {
                ctx.emit(new CaveVillagerMove(current, 2));
                ctx.emit(new CellStateTransition(current, CellState.OPEN, CellState.CLOSED));
            }

            if (current == goal) {
                found = true;
                break;
            }

            for (int neighbor : neighbors(current, size)) {
                if (neighbor < 0 || cells[neighbor] == WALL || closed[neighbor]) continue;
                int tentativeG = g[current] + 1;
                if (tentativeG >= g[neighbor]) continue;

                parent[neighbor] = current;
                g[neighbor] = tentativeG;
                int h = heuristic(neighbor, goal);
                frontier.add(new Node(neighbor, tentativeG + h, h));

                if (!seenOpen[neighbor] && neighbor != start && neighbor != goal) {
                    ctx.emit(new CellStateTransition(neighbor, CellState.DEFAULT, CellState.OPEN));
                }
                seenOpen[neighbor] = true;
            }
        }

        if (!found) {
            ctx.emit(new Message("A* cave: no path found", Message.MessageType.ERROR));
            return;
        }

        int pathCursor = goal;
        while (pathCursor != -1) {
            if (pathCursor != start && pathCursor != goal) {
                ctx.emit(new CaveVillagerMove(pathCursor, 2));
                CellState previous = closed[pathCursor] ? CellState.CLOSED : CellState.OPEN;
                ctx.emit(new CellStateTransition(pathCursor, previous, CellState.PATH));
            }
            pathCursor = parent[pathCursor];
        }

        ctx.emit(new Message("A* cave: path found", Message.MessageType.SUCCESS));
    }

    private int[] neighbors(int index, int size) {
        int x = index % columns;
        int y = (index / columns) % layers;
        int z = index / (columns * layers);

        return new int[] {
                index(x - 1, y, z, size),
                index(x + 1, y, z, size),
                index(x, y - 1, z, size),
                index(x, y + 1, z, size),
                index(x, y, z - 1, size),
                index(x, y, z + 1, size)
        };
    }

    private int heuristic(int from, int to) {
        int fx = from % columns;
        int fy = (from / columns) % layers;
        int fz = from / (columns * layers);
        int tx = to % columns;
        int ty = (to / columns) % layers;
        int tz = to / (columns * layers);
        return Math.abs(fx - tx) + Math.abs(fy - ty) + Math.abs(fz - tz);
    }

    private int index(int x, int y, int z, int size) {
        if (x < 0 || x >= columns || y < 0 || y >= layers || z < 0 || z >= depth) {
            return -1;
        }
        int idx = (z * columns * layers) + (y * columns) + x;
        return idx < size ? idx : -1;
    }

    private record Node(int index, int f, int h) {}
}

