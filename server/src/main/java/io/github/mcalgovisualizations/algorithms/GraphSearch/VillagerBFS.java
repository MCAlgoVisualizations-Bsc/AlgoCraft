package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.PathFound;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class VillagerBFS implements IPlayerSort<NodeContext> {

    @Override
    public void run(NodeContext context) {
        Node root = context.values;
        if (root == null) return;

        Queue<Node> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Node> parents = new HashMap<>();

        // Initialize BFS
        queue.add(root);
        visited.add(root.getID());
        parents.put(root.getID(), null);
System.out.println("--- Starting BFS: Targeting 'End' Node ---");

        while (!queue.isEmpty()) {
            // 1. Pull the next node from the frontier
            Node cursor = queue.poll();

            // 2. Visualize: Highlight the node being currently processed
            // We use -1 for the target value since we are looking for a Status
            context.emit(new Compare(cursor.getID(), -1, cursor.getValue(), -1));

            // 3. Check for Victory Condition (End Status)
            if (cursor.getStatus() == Node.NodeTarget.End) {
                System.out.println("--- Goal Reached! Reconstructing Path ---");
                emitFinalPath(cursor, parents, context);
                return;
            }

            // 4. Explore Neighbors
            for (Node neighbor : cursor.getNeighbors()) {
                if (!visited.contains(neighbor.getID())) {
                    visited.add(neighbor.getID());
                    parents.put(neighbor.getID(), cursor);
                    queue.add(neighbor);

                    // 5. Visual "Ripple": Highlight discovered neighbors immediately
                    // This shows the BFS frontier expanding outward layer-by-layer
                    context.emit(new Compare(neighbor.getID(), -1, neighbor.getValue(), -1));
                }
            }
        }
    }

    /**
     * Reconstructs the path from Start to End using the parent map
     * and emits the PathFound event to draw the final solution.
     */
    private void emitFinalPath(Node target, Map<Integer, Node> parents, NodeContext context) {
        List<Node> finalPath = new ArrayList<>();
        Node pathCursor = target;

        while (pathCursor != null) {
            finalPath.add(pathCursor);
            pathCursor = parents.get(pathCursor.getID());
        }

        Collections.reverse(finalPath);
        context.emit(new PathFound(finalPath));
    }
}