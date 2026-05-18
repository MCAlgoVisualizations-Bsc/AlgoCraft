package io.github.mcalgovisualizations.prefab.algorithms.GraphSearch;

import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.prefab.events.PathFound; // Changed import
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class VillagerDFS implements IPlayerSort<NodeContext> {

    @Override
    public void run(NodeContext context) {
        Node root = context.values;
        if (root == null) return;

        Stack<Node> stack = new Stack<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Node> parents = new HashMap<>();

        stack.push(root);
        visited.add(root.getID());
        parents.put(root.getID(), null);

        Node lastVisited = root;

        while (!stack.isEmpty()) {
            Node cursor = stack.pop();

            // Visualization: Move "camera" or "character" to current node
            List<Node> movementPath = findPathBetween(lastVisited, cursor, parents);
            for (Node pathNode : movementPath) {
                if (pathNode.getID() == lastVisited.getID()) continue;
                // Using -1 for targetValue as we are now looking for a status, not a number
                context.emit(new Compare(pathNode.getID(), -1, pathNode.getValue(), -1));
            }

            lastVisited = cursor;

            // NEW CHECK: Look for the 'End' status assigned in NodeUtils
            if (cursor.getStatus() == Node.NodeTarget.End) {
                emitPathFound(cursor, parents, context);
                return;
            }

            for (Node neighbor : cursor.getNeighbors()) {
                if (!visited.contains(neighbor.getID())) {
                    visited.add(neighbor.getID());
                    parents.put(neighbor.getID(), cursor);
                    stack.push(neighbor);
                }
            }
        }
    }

    private void emitPathFound(Node target, Map<Integer, Node> parents, NodeContext context) {
        List<Node> finalPath = new ArrayList<>();
        Node pathCursor = target;
        while (pathCursor != null) {
            finalPath.add(pathCursor);
            pathCursor = parents.get(pathCursor.getID());
        }
        Collections.reverse(finalPath);
        context.emit(new PathFound(finalPath));
    }

    private List<Node> findPathBetween(Node start, Node end, Map<Integer, Node> parents) {
        if (start.getID() == end.getID()) return Collections.singletonList(start);

        List<Node> startToRoot = new ArrayList<>();
        Node curr = start;
        while (curr != null) {
            startToRoot.add(curr);
            curr = parents.get(curr.getID());
        }

        List<Node> endToRoot = new ArrayList<>();
        curr = end;
        while (curr != null) {
            endToRoot.add(curr);
            curr = parents.get(curr.getID());
        }

        Node lca = null;
        int i = startToRoot.size() - 1;
        int j = endToRoot.size() - 1;
        while (i >= 0 && j >= 0 && startToRoot.get(i).getID() == endToRoot.get(j).getID()) {
            lca = startToRoot.get(i);
            i--;
            j--;
        }

        List<Node> fullPath = new ArrayList<>();
        // Walk up from start to LCA
        for (int k = 0; k <= startToRoot.indexOf(lca); k++) {
            fullPath.add(startToRoot.get(k));
        }
        // Walk down from LCA to end
        for (int k = endToRoot.indexOf(lca) - 1; k >= 0; k--) {
            fullPath.add(endToRoot.get(k));
        }
        return fullPath;
    }
}
