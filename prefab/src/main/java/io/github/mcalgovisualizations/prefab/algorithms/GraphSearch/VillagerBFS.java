package io.github.mcalgovisualizations.prefab.algorithms.GraphSearch;

import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.prefab.events.PathFound;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class VillagerBFS implements IPlayerSort<NodeContext> {

    private Node villagerPosition = null;

    @Override
    public void run(NodeContext context) {
        Node root = context.values;
        if (root == null) return;

        Queue<Node> queue = new LinkedList<>();
        Set<Integer> visitedNodes = new HashSet<>();
        Set<EdgeKey> visitedEdges = new HashSet<>();
        Map<Integer, Node> parents = new HashMap<>();

        // Initialize BFS
        queue.add(root);
        visitedNodes.add(root.getID());
        parents.put(root.getID(), null);
        villagerPosition = root;

        // Initial position visualization
        context.emit(new Compare(root.getID(), -1, root.getValue(), -1));

        // Check if root is the target
        if (root.getStatus() == Node.NodeTarget.End) {
            emitFinalPath(root, parents, context);
            return;
        }

        while (!queue.isEmpty()) {
            // 1. Pull the next node from the frontier
            Node cursor = queue.poll();

            // In true BFS, "visit/check target" happens when dequeued.
            if (cursor.getStatus() == Node.NodeTarget.End) {
                emitFinalPath(cursor, parents, context);
                return;
            }

            // 2. Explore Neighbors
            List<Node> orderedNeighbors = new ArrayList<>(cursor.getNeighbors());
            orderedNeighbors.sort(
                    Comparator
                            .comparing((Node n) -> !visitedNodes.contains(n.getID()))
                            .thenComparingInt(Node::getValue)
                            .thenComparingInt(Node::getID));

            boolean hasUninspectedIncidentEdge = false;
            for (Node neighbor : orderedNeighbors) {
                if (!visitedEdges.contains(EdgeKey.of(cursor, neighbor))) {
                    hasUninspectedIncidentEdge = true;
                    break;
                }
            }
            if (hasUninspectedIncidentEdge) {
                moveToCursor(cursor, parents, context);
            }

            for (int i = 0; i < orderedNeighbors.size(); i++) {
                Node neighbor = orderedNeighbors.get(i);

                if (neighbor.getStatus() == Node.NodeTarget.End) {
                    parents.put(neighbor.getID(), cursor); // Link it first
                    hopTo(neighbor, context);              // Move to it visually
                    emitFinalPath(neighbor, parents, context);
                    return; // Terminate the entire algorithm
                }

                boolean isNewNode = visitedNodes.add(neighbor.getID());
                if (isNewNode) {
                    parents.put(neighbor.getID(), cursor);
                    queue.add(neighbor);
                }

                // Edge-level tracking: inspect each undirected edge exactly once.
                EdgeKey edge = EdgeKey.of(cursor, neighbor);
                if (visitedEdges.add(edge)) {
                    hopTo(neighbor, context);
                    if (hasPendingUninspectedEdges(cursor, orderedNeighbors, i + 1, visitedEdges)) {
                        hopTo(cursor, context);
                    }
                }
            }
        }
    }

    /**
     * Moves the villager from the current position to the target node
     * as a single visualization hop.
     */
    private void hopTo(Node target, NodeContext context) {
        if (villagerPosition == null || villagerPosition.equals(target)) return;
        context.emit(new Compare(target.getID(), -1, target.getValue(), -1));
        villagerPosition = target;
    }

    private void moveToCursor(Node target, Map<Integer, Node> parents, NodeContext context) {
        if (villagerPosition == null || villagerPosition.equals(target)) return;

        List<Node> pathFromRootToCurrent = getPathFromRoot(villagerPosition, parents);
        List<Node> pathFromRootToTarget = getPathFromRoot(target, parents);

        int lcaIndex = 0;
        int minSize = Math.min(pathFromRootToCurrent.size(), pathFromRootToTarget.size());
        while (lcaIndex < minSize && pathFromRootToCurrent.get(lcaIndex).equals(pathFromRootToTarget.get(lcaIndex))) {
            lcaIndex++;
        }
        lcaIndex--;

        for (int i = pathFromRootToCurrent.size() - 2; i >= lcaIndex; i--) {
            Node step = pathFromRootToCurrent.get(i);
            context.emit(new Compare(step.getID(), -1, step.getValue(), -1));
        }

        for (int i = lcaIndex + 1; i < pathFromRootToTarget.size(); i++) {
            Node step = pathFromRootToTarget.get(i);
            context.emit(new Compare(step.getID(), -1, step.getValue(), -1));
        }

        villagerPosition = target;
    }

    private List<Node> getPathFromRoot(Node node, Map<Integer, Node> parents) {
        List<Node> path = new ArrayList<>();
        Node curr = node;
        while (curr != null) {
            path.add(curr);
            curr = parents.get(curr.getID());
        }
        Collections.reverse(path);
        return path;
    }

    private boolean hasPendingUninspectedEdges(Node cursor, List<Node> orderedNeighbors, int startIndex,
                                               Set<EdgeKey> visitedEdges) {
        for (int i = startIndex; i < orderedNeighbors.size(); i++) {
            if (!visitedEdges.contains(EdgeKey.of(cursor, orderedNeighbors.get(i)))) {
                return true;
            }
        }
        return false;
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

    private record EdgeKey(int a, int b) {
        static EdgeKey of(Node first, Node second) {
            int firstId = first.getID();
            int secondId = second.getID();
            return firstId <= secondId ? new EdgeKey(firstId, secondId) : new EdgeKey(secondId, firstId);
        }
    }
}
