package io.github.mcalgovisualizations.prefab.scenes;

import io.github.mcalgovisualizations.prefab.algorithms.mazes.Scenes.ILocatorBarScene;
import io.github.mcalgovisualizations.prefab.events.CellState;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
@Deprecated
public final class CaveTunnelScene extends AbstractScene implements ILocatorBarScene {
    private static final int CELL_SIZE = 4;

    private final int columns;
    private final int layers;
    private final int depth;

    private final Map<Integer, CellState> slotStates = new HashMap<>();
    private final Map<String, Block> overwrittenBlocks = new HashMap<>();
    private final Set<Player> locatorViewers = new HashSet<>();

    private EntityCreature villagerEntity = null;
    private Pos villagerPos = null;
    private LayoutResult[] layoutResults = null;
    private BossBar locatorBar = null;
    private int startSlot = -1;
    private int goalSlot = -1;
    private int initialDistance = -1;
    private int previousDistance = -1;

    public CaveTunnelScene(SceneContext context, int columns, int layers, int depth) {
        super(context);
        if (columns <= 0 || layers <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Cave dimensions must be > 0");
        }
        this.columns = columns;
        this.layers = layers;
        this.depth = depth;
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        cleanUp();
        this.layoutResults = layoutResults;
        slotStates.clear();
        startSlot = -1;
        goalSlot = -1;

        if (layoutResults.length != columns * layers * depth) {
            throw new IllegalArgumentException("Cave layout size mismatch");
        }

        for (int slot = 0; slot < layoutResults.length; slot++) {
            CellState initialState = initialCellState(layoutResults[slot].value());
            slotStates.put(slot, initialState);
            if (initialState == CellState.START) {
                startSlot = slot;
            } else if (initialState == CellState.GOAL) {
                goalSlot = slot;
            }
            renderCell(slot, layoutResults[slot].pos(), initialState);
        }

        spawnVillagerAtStart();
    }

    public void moveVillager(int slot) {
        moveVillager(slot, 2);
    }

    public void moveVillager(int slot, int algorithmSpeed) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) {
            return;
        }

        Pos target = toWalkPos(layoutResults[slot].pos());
        if (villagerEntity == null) {
            spawnVillager(target);
            return;
        }

        if (villagerPos != null && Math.abs(target.y() - villagerPos.y()) > 1.0) {
            villagerEntity.teleport(target);
        } else {
            navigateVillager(target, Math.max(0.25f, algorithmSpeed / 2.0f));
        }

        villagerPos = target;
    }

    public void toggleCellState(int slot, CellState first, CellState second) {
        CellState current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        CellState next = current == first ? second : first;
        slotStates.put(slot, next);
        applyCellState(slot, next);
    }

    @Override
    public void initializeLocatorBar() {
        if (layoutResults == null || startSlot < 0 || goalSlot < 0) {
            return;
        }

        clearLocatorBar();
        initialDistance = manhattanDistance(startSlot, goalSlot);
        previousDistance = initialDistance;
        locatorBar = BossBar.bossBar(locatorTitle(initialDistance), progressForDistance(initialDistance), BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        for (Player viewer : locatorViewers) {
            viewer.showBossBar(locatorBar);
        }
    }

    @Override
    public void updateLocatorBar(int activeSlot) {
        if (locatorBar == null || layoutResults == null || goalSlot < 0 || activeSlot < 0 || activeSlot >= layoutResults.length) {
            return;
        }

        int currentDistance = manhattanDistance(activeSlot, goalSlot);
        BossBar.Color color = currentDistance < previousDistance ? BossBar.Color.GREEN
                : currentDistance > previousDistance ? BossBar.Color.RED
                : BossBar.Color.YELLOW;

        locatorBar.color(color);
        locatorBar.progress(progressForDistance(currentDistance));
        locatorBar.name(locatorTitle(currentDistance));
        previousDistance = currentDistance;
    }

    @Override
    public void setLocatorBarVisible(Player player, boolean visible) {
        if (player == null) return;
        if (visible) {
            locatorViewers.add(player);
            if (locatorBar != null) {
                player.showBossBar(locatorBar);
            }
        } else {
            locatorViewers.remove(player);
            if (locatorBar != null) {
                player.hideBossBar(locatorBar);
            }
        }
    }

    @Override
    public void clearLocatorBar() {
        if (locatorBar != null) {
            for (Player viewer : locatorViewers) {
                viewer.hideBossBar(locatorBar);
            }
        }
        locatorViewers.clear();
        locatorBar = null;
    }


    public Entity cameraTarget() {
        return villagerEntity;
    }

    @Override
    public void cleanUp() {
        clearLocatorBar();
        if (villagerEntity != null) {
            villagerEntity.remove();
            villagerEntity = null;
            villagerPos = null;
        }

        for (String key : overwrittenBlocks.keySet()) {
            instance.setBlock(keyToPos(key), Block.AIR);
        }
        overwrittenBlocks.clear();

        slotStates.clear();
        layoutResults = null;
        startSlot = -1;
        goalSlot = -1;
        initialDistance = -1;
        previousDistance = -1;
        super.cleanUp();
    }

    private void renderCell(int slot, Pos base, CellState state) {
        clearCellVolume(base);
        placeSupportLayer(base);
        applyCellState(slot, state);
    }

    private void applyCellState(int slot, CellState state) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) return;
        Pos base = layoutResults[slot].pos();

        switch (state) {
            case WALL -> renderWallCell(base);
            default -> renderTunnelCell(base, floorForState(state));
        }
    }

    private void renderTunnelCell(Pos base, Block floorBlock) {
        placeBlock(base, floorBlock);
        placeGlassShell(base);
    }

    private void renderWallCell(Pos base) {
        placeBlock(base, Block.BLACKSTONE);
        placeBlock(base.add(0, 1, 0), Block.BLACKSTONE);
        placeGlassShell(base);
    }

    private void placeGlassShell(Pos base) {
        for (int dx = 0; dx < CELL_SIZE; dx++) {
            for (int dz = 0; dz < CELL_SIZE; dz++) {
                for (int dy = 0; dy < CELL_SIZE; dy++) {
                    if (dy == 0) {
                        continue;
                    }
                    boolean boundary = dx == 0 || dx == CELL_SIZE - 1 || dz == 0 || dz == CELL_SIZE - 1 || dy == CELL_SIZE - 1;
                    if (boundary) {
                        placeBlock(base.add(dx, dy, dz), Block.GLASS);
                    }
                }
            }
        }
    }

    private void clearCellVolume(Pos base) {
        for (int dx = 0; dx < CELL_SIZE; dx++) {
            for (int dy = 0; dy < CELL_SIZE; dy++) {
                for (int dz = 0; dz < CELL_SIZE; dz++) {
                    placeBlock(base.add(dx, dy, dz), Block.AIR);
                }
            }
        }
    }

    private void placeSupportLayer(Pos base) {
        placeBlock(base.add(0, -1, 0), Block.GRASS_BLOCK);
    }

    private static CellState initialCellState(Object value) {
        if (!(value instanceof Integer number)) {
            return CellState.DEFAULT;
        }

        return switch (number) {
            case 1 -> CellState.WALL;
            case 2 -> CellState.START;
            case 3 -> CellState.GOAL;
            default -> CellState.DEFAULT;
        };
    }

    private static Block floorForState(CellState state) {
        return switch (state) {
            case DEFAULT -> Block.LIGHT_GRAY_STAINED_GLASS;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.CYAN_TERRACOTTA;
            case PATH -> Block.GLOWSTONE;
            case WALL -> Block.BLACKSTONE;
        };
    }

    private Pos toWalkPos(Pos base) {
        return new Pos(base.x() + 1.5, base.y() + 1.0, base.z() + 1.5);
    }

    private void spawnVillagerAtStart() {
        if (layoutResults == null) return;
        for (int i = 0; i < layoutResults.length; i++) {
            if (Objects.equals(layoutResults[i].value(), 2)) {
                moveVillager(i, 2);
                break;
            }
        }
    }

    private void spawnVillager(Pos pos) {
        if (villagerEntity != null) {
            villagerEntity.remove();
        }
        villagerEntity = new EntityCreature(EntityType.VILLAGER);
        villagerEntity.setInstance(instance, pos);
        villagerPos = pos;
    }

    private void navigateVillager(Pos pos, float speed) {
        if (villagerEntity == null) {
            spawnVillager(pos);
            return;
        }

        var navigator = villagerEntity.getNavigator();
        boolean invoked = false;
        for (Method method : navigator.getClass().getMethods()) {
            if (!method.getName().equals("setPathTo") || method.getParameterCount() != 2) continue;
            Class<?>[] params = method.getParameterTypes();
            if (!params[0].isAssignableFrom(Pos.class)) continue;
            if (!(params[1] == float.class || params[1] == Float.class || params[1] == double.class || params[1] == Double.class || params[1] == int.class || params[1] == Integer.class)) {
                continue;
            }
            try {
                method.invoke(navigator, pos, speed);
                invoked = true;
                break;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // fall back below
            }
        }

        if (!invoked) {
            navigator.setPathTo(pos);
        }
    }

    private int manhattanDistance(int a, int b) {
        if (layoutResults == null || a < 0 || b < 0 || a >= layoutResults.length || b >= layoutResults.length) {
            return 0;
        }

        Pos pa = layoutResults[a].pos();
        Pos pb = layoutResults[b].pos();
        return Math.abs(pa.blockX() - pb.blockX()) + Math.abs(pa.blockY() - pb.blockY()) + Math.abs(pa.blockZ() - pb.blockZ());
    }

    private float progressForDistance(int distance) {
        if (initialDistance <= 0) return 1.0f;
        return Math.clamp(1.0f - (distance / (float) initialDistance), 0.0f, 1.0f);
    }

    private Component locatorTitle(int distance) {
        return Component.text("Distance to goal: " + distance + " (cave)");
    }

    private void placeBlock(Pos pos, Block block) {
        if (pos == null || block == null) return;
        overwrittenBlocks.putIfAbsent(key(pos), block);
        instance.setBlock(pos, block);
    }

    private static String key(Pos pos) {
        return pos.blockX() + ":" + pos.blockY() + ":" + pos.blockZ();
    }

    private static Pos keyToPos(String key) {
        String[] parts = key.split(":");
        return new Pos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}




