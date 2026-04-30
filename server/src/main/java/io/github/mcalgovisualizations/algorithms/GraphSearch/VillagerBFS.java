package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Message;
import io.github.mcalgovisualizations.events.PathFound;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.*;

public final class VillagerBFS implements IPlayerSort<NodeContext> {

    private Node villagerPosition = null;

    @Override
    public void run(NodeContext context) {
        Node root = context.values;
        if (root == null) return;

        Queue<Node> queue = new LinkedList<>();
        Map<Integer, Node> parents = new HashMap<>();

        // Initialize BFS
        queue.add(root);
        root.visited = true;
        parents.put(root.getID(), null);
        villagerPosition = root;

        System.out.println("--- Starting BFS: Targeting 'End' Node ---");

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


            // Move villager to the node being currently processed.
            // moveTo handles emitting path steps if needed.
            moveTo(cursor, parents, context);

            if (cursor.getNeighbors().isEmpty()) {
                context.emit(new Message("No neighbors", Message.MessageType.INFO));
            }
            // 2. Explore Neighbors
            for (Node neighbor : cursor.getNeighbors()) {
                if (neighbor.visited) continue;
                neighbor.visited = true;
                parents.put(neighbor.getID(), cursor);
                queue.add(neighbor);

                // 3. Physical Exploration: Move to neighbor
                moveTo(neighbor, parents, context);

                // Check for Victory Condition immediately upon discovery
                if (neighbor.getStatus() == Node.NodeTarget.End) {
                    System.out.println("--- Goal Reached! Reconstructing Path ---");
                    emitFinalPath(neighbor, parents, context);
                    return;
                }


                // 4. Move back to cursor to continue exploration ONLY if there are more unvisited neighbors
                boolean hasMoreUnvisitedNeighborsFromCursor = false;
                for (Node otherNeighbor : cursor.getNeighbors()) {
                    if (!otherNeighbor.visited) { // Check if it's unvisited
                        // If it's not the current neighbor we just processed, then it's another unvisited neighbor
                        if (!otherNeighbor.equals(neighbor)) {
                            hasMoreUnvisitedNeighborsFromCursor = true;
                            break;
                        }
                    }
                }

                if (hasMoreUnvisitedNeighborsFromCursor) {
                    moveTo(cursor, parents, context);
                }
            }
        }
    }

    /**
     * Moves the villager from the current position to the target node
     * by following the path in the BFS tree.
     */
    private void moveTo(Node target, Map<Integer, Node> parents, NodeContext context) {
        if (villagerPosition == null || villagerPosition.equals(target)) return;

        // 1. Get path from root to villagerPosition
        List<Node> pathFromRootToCurrent = getPathFromRoot(villagerPosition, parents);
        // 2. Get path from root to target
        List<Node> pathFromRootToTarget = getPathFromRoot(target, parents);

        // 3. Find Lowest Common Ancestor (LCA)
        int lcaIndex = 0;
        int minSize = Math.min(pathFromRootToCurrent.size(), pathFromRootToTarget.size());
        while (lcaIndex < minSize && pathFromRootToCurrent.get(lcaIndex).equals(pathFromRootToTarget.get(lcaIndex))) {
            lcaIndex++;
        }
        lcaIndex--; // Last common element

        // 4. Move up from current position to LCA
        for (int i = pathFromRootToCurrent.size() - 2; i >= lcaIndex; i--) {
            Node step = pathFromRootToCurrent.get(i);
            context.emit(new Compare(step.getID(), -1, step.getValue(), -1));
        }

        // 5. Move down from LCA to target
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