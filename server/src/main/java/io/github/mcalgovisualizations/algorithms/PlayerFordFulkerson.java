package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.Arrays;

public final class PlayerFordFulkerson implements IPlayerSort {

    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size == 0) {
            values.emit(new Message("Ford-Fulkerson: empty graph", Message.MessageType.ERROR));
            return;
        }

        int nodeCount = (int) Math.sqrt(size);
        if (nodeCount * nodeCount != size) {
            values.emit(new Message("Ford-Fulkerson: expected flattened NxN capacity matrix", Message.MessageType.ERROR));
            return;
        }

        int[][] residual = new int[nodeCount][nodeCount];
        for (int i = 0; i < size; i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer cap) || cap < 0) {
                values.emit(new Message("Ford-Fulkerson: capacities must be non-negative integers", Message.MessageType.ERROR));
                return;
            }
            residual[i / nodeCount][i % nodeCount] = cap;
        }

        int source = 0;
        int sink = nodeCount - 1;
        int maxFlow = 0;
        int[] parent = new int[nodeCount];

        while (findAugmentingPathDfs(residual, source, sink, parent)) {
            int bottleneck = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                bottleneck = Math.min(bottleneck, residual[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= bottleneck;
                residual[v][u] += bottleneck;
            }

            maxFlow += bottleneck;
            values.emit(new Message(
                    "Augmenting path flow = " + bottleneck + ", total max flow = " + maxFlow,
                    Message.MessageType.INFO
            ));
        }

        values.emit(new Message("Ford-Fulkerson complete. Max flow = " + maxFlow, Message.MessageType.SUCCESS));
    }

    @Override
    public String getName() {
        return "Ford-Fulkerson";
    }

    private static boolean findAugmentingPathDfs(int[][] residual, int source, int sink, int[] parent) {
        Arrays.fill(parent, -1);
        parent[source] = source;
        return dfs(residual, source, sink, parent, new boolean[residual.length]);
    }

    private static boolean dfs(int[][] residual, int u, int sink, int[] parent, boolean[] visited) {
        if (u == sink) {
            return true;
        }

        visited[u] = true;
        for (int v = 0; v < residual.length; v++) {
            if (visited[v] || residual[u][v] <= 0) {
                continue;
            }
            parent[v] = u;
            if (dfs(residual, v, sink, parent, visited)) {
                return true;
            }
        }

        return false;
    }
}

