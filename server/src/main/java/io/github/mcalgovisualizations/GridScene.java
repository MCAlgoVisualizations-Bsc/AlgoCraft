package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.Displays.MobDisplay;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
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

/**
 * 2D hedge-maze scene for pathfinding visualizations.
 */
public class GridScene extends AbstractScene implements ILocatorBarScene {
    private final Map<Integer, CellState> slotStates = new HashMap<>();
    private final Map<String, Block> overwrittenBlocks = new HashMap<>();
    private final Set<Player> locatorViewers = new HashSet<>();
    private EntityCreature villagerEntity = null;
    private LayoutResult[] layoutResults = null;
    private BossBar locatorBar = null;
    private int startSlot = -1;
    private int goalSlot = -1;
    private int inferredColumns = 1;
    private int initialDistance = -1;
    private int previousDistance = -1;

    public GridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        cleanUp();
        this.layoutResults = layoutResults;
        slotStates.clear();
        startSlot = -1;
        goalSlot = -1;
        inferredColumns = inferColumns(layoutResults);

        boolean useBlockGridDisplay = isAStarGrid(layoutResults);

        for (int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            if (useBlockGridDisplay) {
                CellState initialState = initialCellState(value);
                slotStates.put(i, initialState);
                if (initialState == CellState.START) {
                    startSlot = i;
                } else if (initialState == CellState.GOAL) {
                    goalSlot = i;
                }

                renderCell(i, pos, initialState);
            } else {
                IDisplayValue dv = new MobDisplay(pos, value.toString());
                displaysBySlot.put(i, dv);
                dv.setInstance(instance);
            }
        }

        if (useBlockGridDisplay) {
            spawnVillagerAtStart();
        }
    }

    public void moveVillager(int slot) {
        moveVillager(slot, 2);
    }

    public void moveVillager(int slot, int algorithmSpeed) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) {
            return;
        }

        Pos target = toEntityWalkPos(layoutResults[slot].pos());
        if (villagerEntity == null) {
            spawnVillagerAtPos(target);
            return;
        }

        navigateVillager(target, Math.max(0.25f, algorithmSpeed / 2.0f));
    }

    public void toggleCellState(int slot, CellState first, CellState second) {
        var current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        var next = current == first ? second : first;
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
        BossBar.Color color = currentDistance < previousDistance
                ? BossBar.Color.GREEN
                : currentDistance > previousDistance
                ? BossBar.Color.RED
                : BossBar.Color.YELLOW;

        locatorBar.color(color);
        locatorBar.progress(progressForDistance(currentDistance));
        locatorBar.name(locatorTitle(currentDistance));
        previousDistance = currentDistance;
    }

    @Override
    public void setLocatorBarVisible(Player player, boolean visible) {
        if (player == null || locatorBar == null) {
            if (player != null && locatorBar != null) {
                player.hideBossBar(locatorBar);
            }
            return;
        }

        if (visible) {
            locatorViewers.add(player);
            player.showBossBar(locatorBar);
        } else {
            locatorViewers.remove(player);
            player.hideBossBar(locatorBar);
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

    @Override
    public Entity cameraTarget() {
        return villagerEntity;
    }

    @Override
    public void cleanUp() {
        clearLocatorBar();

        if (villagerEntity != null) {
            villagerEntity.remove();
            villagerEntity = null;
        }

        for (var entry : overwrittenBlocks.entrySet()) {
            var pos = keyToPos(entry.getKey());
            instance.setBlock(pos, Block.AIR);
        }
        overwrittenBlocks.clear();

        slotStates.clear();
        layoutResults = null;
        startSlot = -1;
        goalSlot = -1;
        inferredColumns = 1;
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
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) {
            return;
        }

        Pos base = layoutResults[slot].pos();
        switch (state) {
            case WALL -> placeWallColumn(base);
            default -> {
                clearWallColumn(base);
                placeBlock(base, blockForState(state));
            }
        }
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

    private static Block blockForState(CellState state) {
        return switch (state) {
            case DEFAULT -> Block.GRASS_BLOCK;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.CYAN_TERRACOTTA;
            case PATH -> Block.GLOWSTONE;
            case WALL -> Block.OAK_LEAVES;
        };
    }

    private boolean isAStarGrid(LayoutResult[] layoutResults) {
        if (layoutResults == null || layoutResults.length == 0) return false;
        for (var layoutResult : layoutResults) {
            Object raw = layoutResult.value();
            if (!(raw instanceof Integer number)) return false;
            if (number < 0 || number > 3) return false;
        }
        return true;
    }

    private int inferColumns(LayoutResult[] layoutResults) {
        if (layoutResults == null || layoutResults.length <= 1) {
            return 1;
        }

        double firstY = layoutResults[0].pos().y();
        int columns = 1;
        for (int i = 1; i < layoutResults.length; i++) {
            if (Math.abs(layoutResults[i].pos().y() - firstY) < 0.001) {
                columns++;
            } else {
                break;
            }
        }
        return Math.max(1, columns);
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
        if (initialDistance <= 0) {
            return 1.0f;
        }
        return Math.clamp(1.0f - (distance / (float) initialDistance), 0.0f, 1.0f);
    }

    private Component locatorTitle(int distance) {
        return Component.text("Distance to goal: " + distance + " (maze)");
    }

    private Pos toEntityWalkPos(Pos cellPos) {
        return new Pos(cellPos.x() + 0.5, cellPos.y() + 1.0, cellPos.z() + 0.5);
    }

    private void spawnVillagerAtStart() {
        if (layoutResults == null || layoutResults.length == 0) {
            return;
        }

        for (int i = 0; i < layoutResults.length; i++) {
            if (Objects.equals(layoutResults[i].value(), 2)) {
                moveVillager(i, 2);
                break;
            }
        }
    }

    private void spawnVillagerAtPos(Pos pos) {
        if (villagerEntity != null) {
            villagerEntity.remove();
        }

        villagerEntity = new EntityCreature(EntityType.VILLAGER);
        villagerEntity.setInstance(instance, pos);
    }

    private void navigateVillager(Pos pos, float speed) {
        if (villagerEntity == null) {
            spawnVillagerAtPos(pos);
            return;
        }

        var navigator = villagerEntity.getNavigator();
        boolean invoked = false;
        for (Method method : navigator.getClass().getMethods()) {
            if (!method.getName().equals("setPathTo") || method.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (!params[0].isAssignableFrom(Pos.class)) {
                continue;
            }
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

    private void clearCellVolume(Pos base) {
        for (int dx = 0; dx < 1; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dz = 0; dz < 1; dz++) {
                    placeBlock(base.add(dx, dy, dz), Block.AIR);
                }
            }
        }
    }

    private void placeSupportLayer(Pos base) {
        placeBlock(base.add(0, -1, 0), Block.GRASS_BLOCK);
    }

    private void placeWallColumn(Pos base) {
        placeBlock(base, Block.OAK_LEAVES);
        placeBlock(base.add(0, 1, 0), Block.DARK_OAK_LEAVES);
        placeBlock(base.add(0, 2, 0), Block.DARK_OAK_LEAVES);
    }

    private void clearWallColumn(Pos base) {
        placeBlock(base.add(0, 1, 0), Block.AIR);
        placeBlock(base.add(0, 2, 0), Block.AIR);
    }

    private void placeBlock(Pos pos, Block block) {
        if (pos == null || block == null) {
            return;
        }

        String key = key(pos);
        overwrittenBlocks.putIfAbsent(key, block);
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
