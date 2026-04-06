package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class PlayerMaxFlow implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            values.emit(new Message("Max-Flow: empty graph", Message.MessageType.ERROR));
            return;
        }

        int nodeCount = (int) Math.sqrt(size);
        if (nodeCount * nodeCount != size) {
            values.emit(new Message("Max-Flow: expected flattened NxN capacity matrix", Message.MessageType.ERROR));
            return;
        }

        int[][] residual = new int[nodeCount][nodeCount];
        for (int i = 0; i < size; i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer number) || number < 0) {
                values.emit(new Message("Max-Flow: expected non-negative integer capacities", Message.MessageType.ERROR));
                return;
            }
            residual[i / nodeCount][i % nodeCount] = number;
        }

        int source = 0;
        int sink = nodeCount - 1;
        int maxFlow = 0;

        int[] parent = new int[nodeCount];
        while (bfs(residual, source, sink, parent, values, nodeCount)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;

                int matrixIndex = (u * nodeCount) + v;
                values.emit(new CellStateTransition(matrixIndex, CellState.OPEN, CellState.PATH));
            }

            maxFlow += pathFlow;
            values.emit(new Message(
                    "Max-Flow augment: +" + pathFlow + " (total " + maxFlow + ")",
                    Message.MessageType.INFO
            ));
        }

        values.emit(new Message("Max-Flow complete: " + maxFlow, Message.MessageType.SUCCESS));
    }

    @Override
    public String getName() {
        return "Max Flow (Edmonds-Karp)";
    }

    private static <T extends Comparable<T>> boolean bfs(
            int[][] residual,
            int source,
            int sink,
            int[] parent,
            ISort<T> values,
            int nodeCount
    ) {
        Arrays.fill(parent, -1);
        parent[source] = source;

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (int v = 0; v < nodeCount; v++) {
                if (parent[v] != -1 || residual[u][v] <= 0) {
                    continue;
                }

                parent[v] = u;
                int matrixIndex = (u * nodeCount) + v;
                values.emit(new CellStateTransition(matrixIndex, CellState.DEFAULT, CellState.OPEN));

                if (v == sink) {
                    return true;
                }
                queue.add(v);
            }
        }

        return false;
    }
}

