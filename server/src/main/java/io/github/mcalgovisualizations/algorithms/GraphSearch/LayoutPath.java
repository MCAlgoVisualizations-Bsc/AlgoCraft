package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class LayoutPath implements ILayout<Node> {

    private static final int SCALE = 2;
    private static final int SPACING = 10 * SCALE;
    private final int gridCols;

    private final Block pathBlock = Block.DIRT_PATH;

    public LayoutPath(int gridCols) {
        this.gridCols = gridCols;
    }

    @Override
    public LayoutResult[] compute(Node model, Pos origin, Instance instance) {
        if (model == null) return new LayoutResult[0];

        List<Node> nodes = collectNodes(model);
        if (nodes.isEmpty()) return new LayoutResult[0];

        int maxId = nodes.stream().mapToInt(Node::getID).max().orElse(-1);
        LayoutResult[] results = new LayoutResult[maxId + 1];

        double floorY = Math.floor(origin.y()) - 1;
        Pos floorOrigin = new Pos(origin.x(), floorY, origin.z());
        LayoutGrid layoutGrid = buildPositions(nodes, floorOrigin);
        Map<Integer, Pos> positions = layoutGrid.positions();

        // Clear previous paths/blocks in the expected grid area
        clearArea(floorOrigin, instance, layoutGrid.width(), layoutGrid.height());

        renderGraph(model, instance, results, positions, new HashSet<>());

        return results;
    }

    private void clearArea(Pos origin, Instance instance, int cols, int rows) {
        // Clear a slightly larger area to be safe
        int margin = 5;
        for (int r = -margin; r < rows * SPACING + margin; r++) {
            for (int c = -margin; c < cols * SPACING + margin; c++) {
                instance.setBlock(origin.add(c, 0, r), Block.GRASS_BLOCK);
                // Also clear above to remove potential houses
                for (int y = 1; y < 6; y++) {
                    instance.setBlock(origin.add(c, y, r), Block.AIR);
                }
            }
        }
    }

    private List<Node> collectNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        if (root == null) return nodes;

        Set<Integer> visited = new HashSet<>();
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (!visited.add(node.getID())) continue;
            nodes.add(node);
            for (Node neighbor : node.getNeighbors()) {
                if (!visited.contains(neighbor.getID())) {
                    stack.push(neighbor);
                }
            }
        }
        return nodes;
    }

    private LayoutGrid buildPositions(List<Node> nodes, Pos origin) {
        List<Node> orderedNodes = new ArrayList<>(nodes);
        orderedNodes.sort(Comparator.comparingInt(Node::getValue).thenComparingInt(Node::getID));

        Map<Integer, int[]> gridPositions = new HashMap<>();
        Set<String> occupied = new HashSet<>();

        List<Node> squareCycle = findFourCycle(orderedNodes);
        if (!squareCycle.isEmpty()) {
            place(gridPositions, occupied, squareCycle.get(0), 0, 0);
            place(gridPositions, occupied, squareCycle.get(1), 1, 0);
            place(gridPositions, occupied, squareCycle.get(2), 1, 1);
            place(gridPositions, occupied, squareCycle.get(3), 0, 1);
        }

        List<Node> pending = new ArrayList<>();
        for (Node node : orderedNodes) {
            if (!gridPositions.containsKey(node.getID())) {
                pending.add(node);
            }
        }

        if (gridPositions.isEmpty() && !pending.isEmpty()) {
            Node first = pending.removeFirst();
            place(gridPositions, occupied, first, 0, 0);
        }

        boolean progressed = true;
        while (!pending.isEmpty() && progressed) {
            progressed = false;
            Iterator<Node> iterator = pending.iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                int[] near = findNearPlacedSlot(node, gridPositions, occupied);
                if (near != null) {
                    place(gridPositions, occupied, node, near[0], near[1]);
                    iterator.remove();
                    progressed = true;
                }
            }
        }

        int i = 0;
        for (Node node : pending) {
            while (occupied.contains(key(i % gridCols, i / gridCols))) {
                i++;
            }
            int xIdx = i % gridCols;
            int zIdx = i / gridCols;
            place(gridPositions, occupied, node, xIdx, zIdx);
            i++;
        }

        normalizeGridCoordinates(gridPositions);
        int[] bounds = calculateBounds(gridPositions);

        Map<Integer, Pos> positions = new HashMap<>();
        for (Node node : orderedNodes) {
            int[] gridPos = gridPositions.get(node.getID());
            if (gridPos == null) continue;
            int xIdx = gridPos[0];
            int zIdx = gridPos[1];
            positions.put(node.getID(), origin.add(xIdx * SPACING, 0, zIdx * SPACING));
        }
        return new LayoutGrid(positions, bounds[0], bounds[1]);
    }

    private int[] findNearPlacedSlot(Node node, Map<Integer, int[]> gridPositions, Set<String> occupied) {
        List<int[]> anchors = new ArrayList<>();
        for (Node neighbor : node.getNeighbors()) {
            int[] pos = gridPositions.get(neighbor.getID());
            if (pos != null) anchors.add(pos);
        }
        if (anchors.isEmpty()) return null;

        for (int radius = 1; radius <= 8; radius++) {
            for (int[] anchor : anchors) {
                int ax = anchor[0];
                int az = anchor[1];
                int[][] candidates = new int[][]{
                        {ax + radius, az},
                        {ax, az + radius},
                        {ax - radius, az},
                        {ax, az - radius},
                        {ax + radius, az + radius},
                        {ax + radius, az - radius},
                        {ax - radius, az + radius},
                        {ax - radius, az - radius}
                };
                for (int[] candidate : candidates) {
                    if (!occupied.contains(key(candidate[0], candidate[1]))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private void normalizeGridCoordinates(Map<Integer, int[]> gridPositions) {
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (int[] pos : gridPositions.values()) {
            minX = Math.min(minX, pos[0]);
            minZ = Math.min(minZ, pos[1]);
        }
        if (minX == Integer.MAX_VALUE) return;
        if (minX == 0 && minZ == 0) return;
        for (int[] pos : gridPositions.values()) {
            pos[0] -= minX;
            pos[1] -= minZ;
        }
    }

    private int[] calculateBounds(Map<Integer, int[]> gridPositions) {
        int maxX = 0;
        int maxZ = 0;
        for (int[] pos : gridPositions.values()) {
            maxX = Math.max(maxX, pos[0]);
            maxZ = Math.max(maxZ, pos[1]);
        }
        return new int[]{maxX + 1, maxZ + 1};
    }

    private List<Node> findFourCycle(List<Node> nodes) {
        for (Node a : nodes) {
            List<Node> aNeighbors = new ArrayList<>(a.getNeighbors());
            aNeighbors.sort(Comparator.comparingInt(Node::getValue).thenComparingInt(Node::getID));
            for (Node b : aNeighbors) {
                if (b.equals(a)) continue;
                List<Node> bNeighbors = new ArrayList<>(b.getNeighbors());
                bNeighbors.sort(Comparator.comparingInt(Node::getValue).thenComparingInt(Node::getID));
                for (Node c : bNeighbors) {
                    if (c.equals(a) || c.equals(b)) continue;
                    List<Node> cNeighbors = new ArrayList<>(c.getNeighbors());
                    cNeighbors.sort(Comparator.comparingInt(Node::getValue).thenComparingInt(Node::getID));
                    for (Node d : cNeighbors) {
                        if (d.equals(a) || d.equals(b) || d.equals(c)) continue;
                        if (d.getNeighbors().contains(a)) {
                            return List.of(a, b, c, d);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private void place(Map<Integer, int[]> gridPositions, Set<String> occupied, Node node, int xIdx, int zIdx) {
        gridPositions.put(node.getID(), new int[]{xIdx, zIdx});
        occupied.add(key(xIdx, zIdx));
    }

    private String key(int xIdx, int zIdx) {
        return xIdx + ":" + zIdx;
    }

    private record LayoutGrid(Map<Integer, Pos> positions, int width, int height) {}

    private void renderGraph(Node node, Instance instance, LayoutResult[] results,
                             Map<Integer, Pos> positions, Set<Integer> visited) {

        if (node == null || visited.contains(node.getID())) return;
        visited.add(node.getID());

        Pos currentPos = positions.get(node.getID());
        if (currentPos == null) return;

        // --- NEW LOGIC: Block Selection based on Status ---
        Block nodeBlock;
        switch (node.getStatus()) {
            case Start -> {
                    nodeBlock = Block.LIME_WOOL;
                    instance.setBlock(currentPos.add(0,10,0),Block.LIME_WOOL);
            }  // Green for Start
            case End   -> {
                nodeBlock = Block.RED_WOOL;   // Red for End
                buildSmallHouse(currentPos, instance);
            }
            default    -> nodeBlock = Block.WHITE_WOOL; // Default
        }

        instance.setBlock(currentPos, nodeBlock);
        // --------------------------------------------------

        results[node.getID()] = new LayoutResult(
                node.getValue(),
                currentPos,
                new NodeDisplay(String.valueOf(node.getValue()), currentPos)
        );

        for (Node neighbor : node.getNeighbors()) {
            Pos neighborPos = positions.get(neighbor.getID());
            if (neighborPos != null) {
                drawConnection(currentPos, neighborPos, instance, pathBlock);
            }

            if (!visited.contains(neighbor.getID())) {
                renderGraph(neighbor, instance, results, positions, visited);
            }
        }
    }

    private void buildSmallHouse(Pos pos, Instance instance) {
        // pos = pos.add(0, 1, 0);
        final int size = 2;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                // Floor
                instance.setBlock(pos.add(x, 0, z), Block.COBBLESTONE);
                // Wall
                if (x == -size || x == size || z == -size || z == size) {
                    instance.setBlock(pos.add(x, 1, z), Block.OAK_PLANKS);
                    instance.setBlock(pos.add(x, 2, z), Block.OAK_PLANKS);
                    instance.setBlock(pos.add(x, 3, z), Block.OAK_PLANKS);
                }
                // Door
                if (x == 0 || z == 0) {
                    instance.setBlock(pos.add(x, 1, z), Block.AIR);
                    instance.setBlock(pos.add(x, 2, z), Block.AIR);
                }
                // Corners
                if (Math.abs(x) == size && Math.abs(z) == size) {
                    instance.setBlock(pos.add(x, 1, z), Block.OAK_LOG);
                    instance.setBlock(pos.add(x, 2, z), Block.OAK_LOG);
                    instance.setBlock(pos.add(x, 3, z), Block.OAK_LOG);
                }

                // Roof
                instance.setBlock(pos.add(x, 4, z), Block.OAK_LOG);
            }
        }
        /*
        instance.setBlock(pos.add(-size - 1, 0, 0), Block.COBBLESTONE_STAIRS.withProperty("facing", "east"));
        instance.setBlock(pos.add(size + 1, 0, 0), Block.COBBLESTONE_STAIRS.withProperty("facing", "west"));
        instance.setBlock(pos.add(0, 0, -size - 1), Block.COBBLESTONE_STAIRS.withProperty("facing", "south"));
        instance.setBlock(pos.add(0, 0, size + 1), Block.COBBLESTONE_STAIRS.withProperty("facing", "north"));
        */
    }

    private void drawConnection(Pos start, Pos end, Instance instance, Block block) {
        double dist = start.distance(end);
        if (dist < 1) return;

        int steps = (int) Math.ceil(dist);
        double dx = end.x() - start.x();
        double dz = end.z() - start.z();
        double length = Math.sqrt(dx * dx + dz * dz);

        double nx = length > 0 ? -dz / length : 0;
        double nz = length > 0 ? dx / length : 0;

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            double centerX = start.x() + dx * ratio;
            double centerZ = start.z() + dz * ratio;

            for (int offset = -1; offset <= 1; offset++) {
                double finalX = centerX + (nx * offset);
                double finalZ = centerZ + (nz * offset);
                instance.setBlock(new Pos(Math.round(finalX), start.y(), Math.round(finalZ)), block);
            }
        }
    }
}
