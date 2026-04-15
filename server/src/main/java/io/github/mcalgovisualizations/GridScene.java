package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.visualization.renderer.IBlockStateDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.Displays.MobDisplay;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;

import java.util.*;

/**
 * Scene = Minestom world state.
 *
 */
public class GridScene extends AbstractScene {
    // Visual state
    private final Map<Integer, CellState> slotStates = new HashMap<>();
    private Entity villagerEntity = null;
    private LayoutResult<?>[] layoutResults = null;

    public GridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult<?>[] layoutResults) {
        cleanUp();
        this.layoutResults = layoutResults;
        boolean useBlockGridDisplay = isAStarGrid(layoutResults);

        for(int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            IDisplayValue dv;
            if (useBlockGridDisplay) {
                CellState initialState = initialCellState(value.value());
                Block initialBlock = blockForState(initialState);
                // Maze cells are pure block visuals; hide numeric labels.
                dv = new BlockDisplay(instance, pos, initialBlock, "maze", false);
                slotStates.put(i, initialState);

                // Create 2-block-high wall if needed
                if (initialState == CellState.WALL) {
                    createTwoBlockWall(pos);
                }
            } else {
                dv = new MobDisplay(pos, value.toString());
            }

            displaysBySlot.put(i, dv);
            dv.setInstance(instance);
        }

        // Spawn villager at start position
        if (useBlockGridDisplay) {
            spawnVillagerAtStart();
        }
    }

    @Override
    public void setValue(int slot, int value) {
        var display = requireDisplay(slot);
        display.setValue(value);
    }

    public void toggleCellState(int slot, CellState first, CellState second) {
        var current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        var next = current == first ? second : first;
        slotStates.put(slot, next);
        applyCellState(slot, next);
    }

    /**
     * Move the villager to represent the current position in the maze.
     */
    public void moveVillager(int slot) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) {
            return;
        }

        Pos targetPos = layoutResults[slot].pos();
        // Offset villager Y to stand on top of the cell
        Pos villagerPos = targetPos.add(0, 0.5, 0);

        if (villagerEntity == null) {
            spawnVillagerAtPos(villagerPos);
        } else {
            villagerEntity.teleport(villagerPos);
        }
    }

    private void spawnVillagerAtStart() {
        if (layoutResults == null || layoutResults.length == 0) {
            return;
        }

        // Find start cell (value = 2)
        int startSlot = -1;
        for (int i = 0; i < layoutResults.length; i++) {
            Object value = layoutResults[i].value().value();
            if (value instanceof Integer num && num == 2) {
                startSlot = i;
                break;
            }
        }

        if (startSlot >= 0) {
            moveVillager(startSlot);
        }
    }

    private void spawnVillagerAtPos(Pos pos) {
        if (villagerEntity != null) {
            villagerEntity.remove();
        }

        villagerEntity = new Entity(EntityType.VILLAGER);
        villagerEntity.setInstance(instance, pos);
    }

    private void createTwoBlockWall(Pos basePos) {
        // Place a second wall block above the first one
        // We need to place it in the world directly
        if (instance != null) {
            Pos abovePos = basePos.add(0, 1, 0);
            instance.setBlock(abovePos, Block.OAK_LEAVES);
        }
    }
    private void applyCellState(int slot, CellState state) {
        var display = requireDisplay(slot);
        if (!(display instanceof IBlockStateDisplay blockDisplay)) {
            return;
        }
        blockDisplay.setBlock(blockForState(state));
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
            case DEFAULT -> Block.SMOOTH_STONE;
            case WALL -> Block.OAK_LEAVES;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.GREEN_CONCRETE;
            case PATH -> Block.ORANGE_CONCRETE;
        };
    }

    private boolean isAStarGrid(LayoutResult<?>[] layoutResults) {
        if (layoutResults.length == 0) return false;
        for (var layoutResult : layoutResults) {
            Object raw = layoutResult.value().value();
            if (!(raw instanceof Integer number)) return false;
            if (number < 0 || number > 3) return false;
        }
        return true;
    }
}
