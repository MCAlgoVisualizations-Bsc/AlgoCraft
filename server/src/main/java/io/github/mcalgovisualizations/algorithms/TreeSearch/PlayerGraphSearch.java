package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class PlayerGraphSearch<I extends Comparable<I>> implements IPlayerSort<NodeContext<I>> {

    @Override
    public void run(NodeContext<I> context) {
        Node<I> root = context.values;
        if (root == null) return;

        I targetValue = findDeepValue(root);

        Queue<Node<I>> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Node<I>> parents = new HashMap<>();

        queue.add(root);
        visited.add(root.id());

        Node<I> lastVisited = root;

        System.out.println("--- Starting BFS Visualization ---");
        System.out.println("Target Value: " + targetValue);

        while (!queue.isEmpty()) {
            Node<I> cursor = queue.poll();

            // Calculate the path from the last position to the current BFS node
            List<Node<I>> path = findPathBetween(lastVisited, cursor, parents);

            if (path.size() > 1) {
                // The first element is where he is, the last is where he's going
                System.out.println("[Movement] Walking from Node ID: " + lastVisited.id() + " to Node ID: " + cursor.id());

                for (Node<I> pathNode : path) {
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

            if (targetValue.equals(cursor.value())) {
                System.out.println("--- Target Found! Stopping search. ---");
                return;
            }

            for (Node<I> neighbor : cursor.neighbors()) {
                if (!visited.contains(neighbor.id())) {
                    visited.add(neighbor.id());
                    parents.put(neighbor.id(), cursor);
                    queue.add(neighbor);
                }
            }
        }
    }

    private List<Node<I>> findPathBetween(Node<I> start, Node<I> end, Map<Integer, Node<I>> parents) {
        if (start.id() == end.id()) return Collections.singletonList(start);

        List<Node<I>> startToRoot = new ArrayList<>();
        Node<I> curr = start;
        while (curr != null) {
            startToRoot.add(curr);
            curr = parents.get(curr.id());
        }

        List<Node<I>> endToRoot = new ArrayList<>();
        curr = end;
        while (curr != null) {
            endToRoot.add(curr);
            curr = parents.get(curr.id());
        }

        Node<I> lca = null;
        int i = startToRoot.size() - 1;
        int j = endToRoot.size() - 1;
        while (i >= 0 && j >= 0 && startToRoot.get(i).id() == endToRoot.get(j).id()) {
            lca = startToRoot.get(i);
            i--;
            j--;
        }

        List<Node<I>> fullPath = new ArrayList<>();
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

    private I findDeepValue(Node<I> root) {
        if (!root.neighbors().isEmpty()) {
            Node<I> child = root.neighbors().get(0);
            if (!child.neighbors().isEmpty()) return child.neighbors().get(0).value();
            return child.value();
        }
        return root.value();
    }
}