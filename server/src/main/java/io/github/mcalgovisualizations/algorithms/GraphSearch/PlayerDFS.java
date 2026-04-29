package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.PathFound; // Changed import
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class PlayerDFS implements IPlayerSort<NodeContext> {

    @Override
    public void run(NodeContext context) {
        Node root = context.values;
        if (root == null) return;

        int targetValue = findDeepValue(root);

        Stack<Node> stack = new Stack<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Node> parents = new HashMap<>();

        stack.push(root);
        visited.add(root.id());
        parents.put(root.id(), null);

        Node lastVisited = root;

        System.out.println("--- Starting DFS Visualization ---");
        System.out.println("Target Value: " + targetValue);

        while (!stack.isEmpty()) {
            Node cursor = stack.pop();

            // Calculate the path from the last position to the current DFS node
            List<Node> movementPath = findPathBetween(lastVisited, cursor, parents);

            if (movementPath.size() > 1) {
                // The first element is where he is, the last is where he's going
                System.out.println("[Movement] Walking from Node ID: " + lastVisited.id() + " to Node ID: " + cursor.id());

                for (Node pathNode : movementPath) {
                    if (pathNode.id() == lastVisited.id()) continue;

                    // Small debug print for every step on the path
                    System.out.println("  -> Stepping onto Node ID: " + pathNode.id());
                    context.emit(new Compare(pathNode.id(), -1, pathNode.value(), targetValue));
                }
            } else {
                // This usually only happens for the very first node (root)
                System.out.println("[Visit] Starting at Root Node ID: " + cursor.id());
                context.emit(new Compare(cursor.id(), -1, cursor.value(), targetValue));
            }

            lastVisited = cursor;

            if (targetValue == cursor.value()) {
                System.out.println("--- Target Found! Stopping search. ---");
                
                // Reconstruct the final path
                List<Node> finalPath = new ArrayList<>();
                Node pathCursor = cursor;
                while (pathCursor != null) {
                    finalPath.add(pathCursor);
                    pathCursor = parents.get(pathCursor.id());
                }
                Collections.reverse(finalPath);

                // Emit the existing PathFound event
                context.emit(new PathFound(finalPath));
                
                return;
            }

            // Push unvisited neighbors onto the stack
            for (Node neighbor : cursor.neighbors()) {
                if (!visited.contains(neighbor.id())) {
                    visited.add(neighbor.id());
                    parents.put(neighbor.id(), cursor);
                    stack.push(neighbor);
                }
            }
        }
    }

    private List<Node> findPathBetween(Node start, Node end, Map<Integer, Node> parents) {
        if (start.id() == end.id()) return Collections.singletonList(start);

        List<Node> startToRoot = new ArrayList<>();
        Node curr = start;
        while (curr != null) {
            startToRoot.add(curr);
            curr = parents.get(curr.id());
        }

        List<Node> endToRoot = new ArrayList<>();
        curr = end;
        while (curr != null) {
            endToRoot.add(curr);
            curr = parents.get(curr.id());
        }

        Node lca = null;
        int i = startToRoot.size() - 1;
        int j = endToRoot.size() - 1;
        while (i >= 0 && j >= 0 && startToRoot.get(i).id() == endToRoot.get(j).id()) {
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

    private int findDeepValue(Node root) {
        // Find a node that is relatively far from the root using a quick BFS
        Queue<Node> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        
        queue.add(root);
        visited.add(root.id());
        
        Node deepestNode = root;
        int count = 0;
        int maxDepth = 20; // limit how deep we go to find a target

        while (!queue.isEmpty() && count < maxDepth) {
            deepestNode = queue.poll();
            for (Node neighbor : deepestNode.neighbors()) {
                if (!visited.contains(neighbor.id())) {
                    visited.add(neighbor.id());
                    queue.add(neighbor);
                }
            }
            count++;
        }
        
        return deepestNode.value();
    }
}
