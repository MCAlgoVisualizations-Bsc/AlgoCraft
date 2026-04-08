package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.events.FlowEdgeFlowUpdate;
import io.github.mcalgovisualizations.events.FlowEdgeVisit;
import io.github.mcalgovisualizations.events.FlowPathEdge;
import io.github.mcalgovisualizations.events.FlowStatus;
import io.github.mcalgovisualizations.layouts.FlowNetworkRules;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.models.ISort;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class PlayerMaxFlow implements IPlayerSort {
    @Override
    public <T extends Comparable<T>> void sort(ISort<T> values) {
        int size = values.size();
        if (size != FlowNetworkRules.MATRIX_SIZE) {
            values.emit(new FlowStatus(FlowStatus.Type.INVALID_INPUT, 0, 0));
            return;
        }

        int nodeCount = FlowNetworkRules.NODE_COUNT;

        int[][] residual = new int[nodeCount][nodeCount];
        int[][] capacity = new int[nodeCount][nodeCount];
        int[][] flow = new int[nodeCount][nodeCount];

        for (int i = 0; i < size; i++) {
            T raw = values.get(i);
            if (!(raw instanceof Integer number) || number < 0) {
                values.emit(new FlowStatus(FlowStatus.Type.INVALID_INPUT, 0, 0));
                return;
            }
            int row = i / nodeCount;
            int col = i % nodeCount;
            if (isAllowedDirectedEdge(row, col, nodeCount)) {
                residual[row][col] = number;
                capacity[row][col] = number;
            } else {
                residual[row][col] = 0;
                capacity[row][col] = 0;
            }
        }

        int source = 0;
        int sink = nodeCount - 1;
        int maxFlow = 0;

        // Initialize node fills and sink total text.
        emitNodeLoads(values, capacity, flow, nodeCount, maxFlow);

        int[] parent = new int[nodeCount];
        while (bfs(residual, source, sink, parent, values, nodeCount)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                int slot = (u * nodeCount) + v;
                values.emit(new FlowPathEdge(nodeName(u), nodeName(v), slot, pathFlow));

                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;

                if (capacity[u][v] > 0) {
                    flow[u][v] += pathFlow;
                    values.emit(new FlowEdgeFlowUpdate(slot, flow[u][v]));
                } else if (capacity[v][u] > 0) {
                    flow[v][u] -= pathFlow;
                    int reverseSlot = (v * nodeCount) + u;
                    values.emit(new FlowEdgeFlowUpdate(reverseSlot, flow[v][u]));
                }
            }

            maxFlow += pathFlow;
            emitNodeLoads(values, capacity, flow, nodeCount, maxFlow);
            values.emit(new FlowStatus(FlowStatus.Type.AUGMENTED, pathFlow, maxFlow));
        }

        values.emit(new FlowStatus(FlowStatus.Type.COMPLETE, maxFlow, maxFlow));
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
                if (parent[v] != -1 || residual[u][v] <= 0)  { //|| !isAllowedDirectedEdge(u, v, nodeCount))
                    continue;
                }

                parent[v] = u;
                int matrixIndex = (u * nodeCount) + v;
                values.emit(new FlowEdgeVisit(nodeName(u), nodeName(v), matrixIndex, residual[u][v]));

                if (v == sink) {
                    return true;
                }
                queue.add(v);
            }
        }

        return false;
    }

    private static <T extends Comparable<T>> void emitNodeLoads(
            ISort<T> values,
            int[][] capacity,
            int[][] flow,
            int nodeCount,
            int maxFlow
    ) {
        for (int node = 0; node < nodeCount; node++) {
            if (node == nodeCount - 1) {
                // F always shows current total max flow value.
                int sinkSlot = (node * nodeCount) + node;
                values.emit(new FlowEdgeFlowUpdate(sinkSlot, maxFlow));
                continue;
            }

            int load = 0;
            for (int to = 0; to < nodeCount; to++) {
                if (capacity[node][to] > 0) {
                    load += Math.max(0, flow[node][to]);
                }
            }
            int slot = (node * nodeCount) + node;
            values.emit(new FlowEdgeFlowUpdate(slot, load));
        }
    }

    private static boolean isAllowedDirectedEdge(int from, int to, int nodeCount) {
        return FlowNetworkRules.isAllowedDirectedEdge(from, to, nodeCount);
    }

    private static char nodeName(int index) {
        return (char) ('A' + index);
    }
}

