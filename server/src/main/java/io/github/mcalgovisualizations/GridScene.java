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
import net.minestom.server.entity.EntityCreature;
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
    private final Map<BlockPos, Block> overwrittenBlocks = new HashMap<>();
    private EntityCreature villagerEntity = null;
    private LayoutResult<?>[] layoutResults = null;

    public GridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult<?>[] layoutResults) {
        cleanUp();
        this.layoutResults = layoutResults;
        slotStates.clear();
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
                placeSupportLayer(pos);

                // Create 2-block-high wall if needed
                if (initialState == CellState.WALL) {
                    placeWallColumn(pos);
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

        Pos villagerPos = toEntityWalkPos(layoutResults[slot].pos());

        if (villagerEntity == null) {
            spawnVillagerAtPos(villagerPos);
        } else {
            villagerEntity.getNavigator().setPathTo(villagerPos);
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

        villagerEntity = new EntityCreature(EntityType.VILLAGER);
        villagerEntity.setInstance(instance, pos);
    }

    @Override
    public void cleanUp() {
        if (villagerEntity != null) {
            villagerEntity.remove();
            villagerEntity = null;
        }
        restoreWorldBlocks();
        slotStates.clear();
        layoutResults = null;
        super.cleanUp();
    }

    private void placeSupportLayer(Pos basePos) {
        placeWorldBlock(basePos.add(0, -1, 0), Block.GRASS_BLOCK);
        placeWorldBlock(basePos, Block.BARRIER);
    }

    private void placeWallColumn(Pos basePos) {
        placeWorldBlock(basePos, Block.DARK_OAK_LEAVES);
        placeWorldBlock(basePos.add(0, 1, 0), Block.DARK_OAK_LEAVES);
        placeWorldBlock(basePos.add(0, 2, 0), Block.DARK_OAK_LEAVES);
        placeWorldBlock(basePos.add(0, 3, 0), Block.DARK_OAK_LEAVES);
    }

    private Pos toEntityWalkPos(Pos cellPos) {
        // Center the villager on the block and stand on top of collision layer.
        return new Pos(cellPos.blockX() + 0.5, cellPos.blockY() + 1.0, cellPos.blockZ() + 0.5);
    }

    private void placeWorldBlock(Pos pos, Block block) {
        BlockPos blockPos = BlockPos.from(pos);
        overwrittenBlocks.putIfAbsent(blockPos, instance.getBlock(blockPos.x, blockPos.y, blockPos.z));
        instance.setBlock(blockPos.x, blockPos.y, blockPos.z, block);
    }

    private void restoreWorldBlocks() {
        for (var entry : overwrittenBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            instance.setBlock(pos.x, pos.y, pos.z, entry.getValue());
        }
        overwrittenBlocks.clear();
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
            case WALL -> Block.DARK_OAK_LEAVES;
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

    private record BlockPos(int x, int y, int z) {
        private static BlockPos from(Pos pos) {
            return new BlockPos(pos.blockX(), pos.blockY(), pos.blockZ());
        }
    }
}
