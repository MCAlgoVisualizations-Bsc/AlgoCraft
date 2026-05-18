package io.github.mcalgovisualizations.prefab.algorithms.GraphSearch;

import io.github.mcalgovisualizations.prefab.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

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

        clearArea(floorOrigin, instance, layoutGrid.width(), layoutGrid.height());

        Set<String> globalBlocked = new HashSet<>();

        // PASS 1: Build nodes and paths
        buildInfrastructure(model, instance, results, positions, new HashSet<>(), globalBlocked);

        // EXTRA: Detect cycles and build fountains in the middle
        List<Node> cycle = findFourCycle(nodes);
        if (!cycle.isEmpty()) {
            buildCycleFountain(cycle, positions, instance, globalBlocked);
        }

        // PASS 2: Landscaping
        decorateGraph(model, instance, positions, new HashSet<>(), globalBlocked);

        return results;
    }

    private void buildCycleFountain(List<Node> cycle, Map<Integer, Pos> positions, Instance instance, Set<String> blocked) {
        double avgX = 0, avgZ = 0, avgY = 0;
        for (Node n : cycle) {
            Pos p = positions.get(n.getID());
            avgX += p.x();
            avgZ += p.z();
            avgY = p.y();
        }
        Pos center = new Pos(Math.round(avgX / cycle.size()), avgY, Math.round(avgZ / cycle.size()));

        // Buffer for the 5x5 fountain
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                String key = key((int)center.x() + x, (int)center.z() + z);
                if (blocked.contains(key)) return;
                blocked.add(key);
            }
        }

        final int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Foundation layer
                instance.setBlock(center.add(x, 0, z), Block.STONE_BRICKS);

                // Central Pillar Logic
                if (x == 0 && z == 0) {
                    instance.setBlock(center.add(0, 1, 0), Block.WATER);
                    instance.setBlock(center.add(0, 2, 0), Block.WATER);
                    // The core of the "+" water feature
                    instance.setBlock(center.add(0, 3, 0), Block.WATER);
                }
                // Creating the "+" water shape at height 3
                else if (Math.abs(x) == 1 && z == 0 || Math.abs(z) == 1 && x == 0) {
                    instance.setBlock(center.add(x, 2, 0), Block.WATER);
                    instance.setBlock(center.add(0, 2, z), Block.WATER);
                    instance.setBlock(center.add(x, 1, z), Block.WATER); // Basin water
                }
                // Fill the rest of the basin with air (or water if you want a full pool)
                else if (Math.abs(x) < radius && Math.abs(z) < radius) {
                    instance.setBlock(center.add(x, 1, z), Block.WATER);
                }
                // The Rim with corrected corner facing
                else {
                    Block stair = Block.STONE_BRICK_STAIRS;
                    if (x == -radius) stair = stair.withProperty("facing", "east");
                    else if (x == radius) stair = stair.withProperty("facing", "west");
                    else if (z == -radius) stair = stair.withProperty("facing", "south");
                    else if (z == radius) stair = stair.withProperty("facing", "north");

                    if (x == -radius && z == -radius) stair = stair.withProperty("shape", "outer_right");
                    else if (x == radius && z == -radius) stair = stair.withProperty("shape", "outer_left");
                    else if (x == -radius && z == radius) stair = stair.withProperty("shape", "outer_left");
                    else if (x == radius && z == radius) stair = stair.withProperty("shape", "outer_right");

                    instance.setBlock(center.add(x, 1, z), stair);
                }
            }
        }
    }

    private void clearArea(Pos origin, Instance instance, int cols, int rows) {
        int margin = 12;
        for (int r = -margin; r < rows * SPACING + margin; r++) {
            for (int c = -margin; c < cols * SPACING + margin; c++) {
                instance.setBlock(origin.add(c, 0, r), Block.GRASS_BLOCK);
                for (int y = 1; y < 8; y++) instance.setBlock(origin.add(c, y, r), Block.AIR);
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
            for (Node neighbor : node.getNeighbors()) if (!visited.contains(neighbor.getID())) stack.push(neighbor);
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
        for (Node node : orderedNodes) if (!gridPositions.containsKey(node.getID())) pending.add(node);
        if (gridPositions.isEmpty() && !pending.isEmpty()) place(gridPositions, occupied, pending.removeFirst(), 0, 0);

        boolean progressed = true;
        while (!pending.isEmpty() && progressed) {
            progressed = false;
            Iterator<Node> it = pending.iterator();
            while (it.hasNext()) {
                Node node = it.next();
                int[] near = findNearPlacedSlot(node, gridPositions, occupied);
                if (near != null) {
                    place(gridPositions, occupied, node, near[0], near[1]);
                    it.remove();
                    progressed = true;
                }
            }
        }

        int i = 0;
        for (Node node : pending) {
            while (occupied.contains(key(i % gridCols, i / gridCols))) i++;
            place(gridPositions, occupied, node, i % gridCols, i / gridCols);
            i++;
        }

        normalizeGridCoordinates(gridPositions);
        int[] bounds = calculateBounds(gridPositions);
        Map<Integer, Pos> positions = new HashMap<>();
        for (Node node : orderedNodes) {
            int[] gp = gridPositions.get(node.getID());
            if (gp != null) positions.put(node.getID(), origin.add(gp[0] * SPACING, 0, gp[1] * SPACING));
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
        for (int r = 1; r <= 8; r++) {
            for (int[] a : anchors) {
                int[][] cands = {{a[0]+r, a[1]}, {a[0], a[1]+r}, {a[0]-r, a[1]}, {a[0], a[1]-r}};
                for (int[] c : cands) if (!occupied.contains(key(c[0], c[1]))) return c;
            }
        }
        return null;
    }

    private void normalizeGridCoordinates(Map<Integer, int[]> gp) {
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (int[] p : gp.values()) { minX = Math.min(minX, p[0]); minZ = Math.min(minZ, p[1]); }
        if (minX == Integer.MAX_VALUE) return;
        for (int[] p : gp.values()) { p[0] -= minX; p[1] -= minZ; }
    }

    private int[] calculateBounds(Map<Integer, int[]> gp) {
        int mx = 0, mz = 0;
        for (int[] p : gp.values()) { mx = Math.max(mx, p[0]); mz = Math.max(mz, p[1]); }
        return new int[]{mx + 1, mz + 1};
    }

    private List<Node> findFourCycle(List<Node> nodes) {
        for (Node a : nodes) {
            for (Node b : a.getNeighbors()) {
                if (b.equals(a)) continue;
                for (Node c : b.getNeighbors()) {
                    if (c.equals(a) || c.equals(b)) continue;
                    for (Node d : c.getNeighbors()) {
                        if (d.equals(a) || d.equals(b) || d.equals(c)) continue;
                        if (d.getNeighbors().contains(a)) return List.of(a, b, c, d);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private void place(Map<Integer, int[]> gp, Set<String> occ, Node n, int x, int z) {
        gp.put(n.getID(), new int[]{x, z});
        occ.add(key(x, z));
    }

    private String key(int x, int z) { return x + ":" + z; }

    private record LayoutGrid(Map<Integer, Pos> positions, int width, int height) {}

    private void buildInfrastructure(Node node, Instance instance, LayoutResult[] results,
                                     Map<Integer, Pos> positions, Set<Integer> visited, Set<String> blocked) {
        if (node == null || visited.contains(node.getID())) return;
        visited.add(node.getID());
        Pos currentPos = positions.get(node.getID());
        if (currentPos == null) return;

        int buffer = 4;
        for (int bx = -buffer; bx <= buffer; bx++) {
            for (int bz = -buffer; bz <= buffer; bz++) blocked.add(key((int)currentPos.x() + bx, (int)currentPos.z() + bz));
        }

        Block nodeBlock = switch (node.getStatus()) {
            case Start -> { instance.setBlock(currentPos.add(0, 10, 0), Block.LIME_WOOL); yield Block.LIME_WOOL; }
            case End -> { buildSmallHouse(currentPos, instance); yield Block.RED_WOOL; }
            default -> Block.WHITE_WOOL;
        };

        instance.setBlock(currentPos, nodeBlock);
        results[node.getID()] = new LayoutResult(node.getValue(), currentPos, new NodeDisplay(String.valueOf(node.getValue()), currentPos));

        for (Node neighbor : node.getNeighbors()) {
            Pos neighborPos = positions.get(neighbor.getID());
            if (neighborPos != null) drawPathOnly(currentPos, neighborPos, instance, pathBlock, blocked);
            buildInfrastructure(neighbor, instance, results, positions, visited, blocked);
        }
    }

    private void drawPathOnly(Pos start, Pos end, Instance instance, Block block, Set<String> blocked) {
        double dist = start.distance(end);
        int steps = (int) Math.ceil(dist);
        double dx = end.x() - start.x(), dz = end.z() - start.z();
        double length = Math.sqrt(dx * dx + dz * dz);
        double nx = length > 0 ? -dz / length : 0, nz = length > 0 ? dx / length : 0;

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            double centerX = start.x() + dx * ratio, centerZ = start.z() + dz * ratio;
            for (int offset = -1; offset <= 1; offset++) {
                int px = (int) Math.round(centerX + (nx * offset));
                int pz = (int) Math.round(centerZ + (nz * offset));
                instance.setBlock(new Pos(px, start.y(), pz), block);
                for (int bx = -1; bx <= 1; bx++) {
                    for (int bz = -1; bz <= 1; bz++) blocked.add(key(px + bx, pz + bz));
                }
            }
        }
    }

    private void decorateGraph(Node node, Instance instance, Map<Integer, Pos> positions,
                               Set<Integer> visited, Set<String> blocked) {
        if (node == null || visited.contains(node.getID())) return;
        visited.add(node.getID());
        Pos currentPos = positions.get(node.getID());
        Set<String> localDecorated = new HashSet<>();

        for (Node neighbor : node.getNeighbors()) {
            Pos neighborPos = positions.get(neighbor.getID());
            if (neighborPos != null) addDecorationsToConnection(currentPos, neighborPos, instance, localDecorated, blocked);
            decorateGraph(neighbor, instance, positions, visited, blocked);
        }
    }

    private void addDecorationsToConnection(Pos start, Pos end, Instance instance, Set<String> decorated, Set<String> blocked) {
        double dist = start.distance(end);
        int steps = (int) Math.ceil(dist);
        double dx = end.x() - start.x(), dz = end.z() - start.z();
        double length = Math.sqrt(dx * dx + dz * dz);
        double nx = length > 0 ? -dz / length : 0, nz = length > 0 ? dx / length : 0;
        double tx = length > 0 ? dx / length : 0, tz = length > 0 ? dz / length : 0;

        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            decorateNearPath(instance, start, start.x() + dx * ratio, start.z() + dz * ratio, nx, nz, tx, tz, i, decorated, blocked);
        }
    }

    private void decorateNearPath(Instance instance, Pos start, double centerX, double centerZ,
                                  double nx, double nz, double tx, double tz, int step, Set<String> decorated,
                                  Set<String> blocked) {
        int[] signs = {-1, 1};
        for (int side : signs) {
            double lateralDistance = 3.2;
            int h = stableHash((int) Math.round(centerX), (int) Math.round(centerZ), step, 101 + side);
            double forwardOffset = (Math.floorMod(h, 3) - 1) * 1.5;
            int x = (int) Math.round(centerX + (nx * lateralDistance * side) + (tx * forwardOffset));
            int z = (int) Math.round(centerZ + (nz * lateralDistance * side) + (tz * forwardOffset));
            int y = (int) Math.round(start.y());

            if (decorated.contains(key(x, z)) || !shouldDecorate(x, z, step, side)) continue;
            if (!canPlaceDecoration(instance, x, y, z, blocked)) continue;

            decorated.add(key(x, z));
            if (shouldPlaceRuin(x, z, step)) placeSmallRuin(instance, x, y, z, decorated, blocked);
            else if (shouldPlaceTree(x, z, step)) placeSmallTree(instance, x, y, z);
            else placeBushCluster(instance, x, y, z, step, decorated, blocked);
        }
    }

    private boolean shouldDecorate(int x, int z, int s, int side) { return Math.floorMod(stableHash(x, z, s, side), 3) == 0; }
    private boolean shouldPlaceTree(int x, int z, int s) { return Math.floorMod(stableHash(x, z, s, 13), 10) == 0; }
    private boolean shouldPlaceRuin(int x, int z, int s) { return Math.floorMod(stableHash(x, z, s, 31), 15) == 0; }

    private int stableHash(int x, int z, int a, int b) {
        int h = x * 73856093 ^ z * 19349663 ^ a * 83492791 ^ b * 265443576;
        return h ^ (h >>> 16);
    }

    private void placeSmallTree(Instance instance, int x, int y, int z) {
        for (int ty = 1; ty <= 3; ty++) instance.setBlock(new Pos(x, y + ty, z), Block.OAK_LOG);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) if (dx != 0 || dz != 0) instance.setBlock(new Pos(x + dx, y + 3, z + dz), Block.OAK_LEAVES);
        }
        instance.setBlock(new Pos(x, y + 4, z), Block.OAK_LEAVES);
    }

    private void placeBushCluster(Instance instance, int x, int y, int z, int step, Set<String> dec, Set<String> block) {
        placeBushLeaf(instance, x, y, z, step, 0, dec, block);
        int[][] neighbors = {{1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {-1,-1}, {1,-1}, {-1,1}};
        for (int i = 0; i < neighbors.length; i++) {
            int nx = x + neighbors[i][0], nz = z + neighbors[i][1];
            if (Math.floorMod(stableHash(nx, nz, step, 61 + i), 10) < 7) placeBushLeaf(instance, nx, y, nz, step, i + 1, dec, block);
        }
    }

    private void placeBushLeaf(Instance instance, int x, int y, int z, int step, int salt, Set<String> dec, Set<String> block) {
        if (block.contains(key(x, z)) || dec.contains(key(x, z)) || !canPlaceDecoration(instance, x, y, z, block)) return;
        dec.add(key(x, z));
        Block leaf = Math.floorMod(stableHash(x, z, step, 97 + salt), 4) == 0 ? Block.FLOWERING_AZALEA_LEAVES : Block.AZALEA_LEAVES;
        instance.setBlock(new Pos(x, y + 1, z), leaf);
        leaf = Math.floorMod(stableHash(x, z, step, 97 + salt + 1), 4) == 0 ? Block.FLOWERING_AZALEA_LEAVES : Block.AZALEA_LEAVES;
        instance.setBlock(new Pos(x, y + 2, z), leaf);
    }

    private boolean canPlaceDecoration(Instance instance, int x, int y, int z, Set<String> blocked) {
        return !blocked.contains(key(x, z)) && instance.getBlock(new Pos(x, y, z)).equals(Block.GRASS_BLOCK)
                && instance.getBlock(new Pos(x, y + 1, z)).equals(Block.AIR);
    }

    private void placeSmallRuin(Instance instance, int x, int y, int z, Set<String> dec, Set<String> block) {
        int[][] foot = {{0,0}, {1,0}, {0,1}};
        int lanternsPlaced = 0;
        for (int[] t : foot) {
            int tx = x + t[0], tz = z + t[1];
            if (!canPlaceDecoration(instance, tx, y, tz, block)) continue;
            dec.add(key(tx, tz));
            instance.setBlock(new Pos(tx, y + 1, tz), Math.floorMod(stableHash(tx, tz, y, 47), 2) == 0 ? Block.COBBLESTONE_WALL : Block.MOSSY_COBBLESTONE_WALL);
            if (lanternsPlaced < 1 && Math.floorMod(stableHash(tx, tz, y, 53), 5) == 0 && instance.getBlock(new Pos(tx, y + 2, tz)).equals(Block.AIR)) {
                instance.setBlock(new Pos(tx, y + 2, tz), Block.LANTERN);
                lanternsPlaced++;
            }
        }
    }

    private void buildSmallHouse(Pos pos, Instance instance) {
        final int size = 2;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                instance.setBlock(pos.add(x, 0, z), Block.COBBLESTONE);
                if (x == -size || x == size || z == -size || z == size) for (int y = 1; y <= 3; y++) instance.setBlock(pos.add(x, y, z), Block.OAK_PLANKS);
                if (x == 0 || z == 0) { instance.setBlock(pos.add(x, 1, z), Block.AIR); instance.setBlock(pos.add(x, 2, z), Block.AIR); }
                if (Math.abs(x) == size && Math.abs(z) == size) for (int y = 1; y <= 3; y++) instance.setBlock(pos.add(x, y, z), Block.OAK_LOG);
                instance.setBlock(pos.add(x, 4, z), Block.OAK_LOG);
            }
        }
    }
}
