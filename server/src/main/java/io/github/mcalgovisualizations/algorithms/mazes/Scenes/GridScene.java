package io.github.mcalgovisualizations.algorithms.mazes.Scenes;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.Displays.MobDisplay;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;

import java.util.*;

public class GridScene extends AbstractScene {
    protected final Map<Integer, CellState> slotStates = new HashMap<>();
    protected final Map<BlockPos, Block> overwrittenBlocks = new HashMap<>();
    protected EntityCreature villagerEntity = null;
    protected LayoutResult[] layoutResults = null;
    protected int startSlot = -1;
    protected int goalSlot = -1;
    protected int inferredColumns = 1;

    public GridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        cleanUp();
        this.layoutResults = layoutResults;
        preloadChunks(layoutResults);
        slotStates.clear();
        startSlot = -1;
        goalSlot = -1;
        inferredColumns = inferColumns(layoutResults);
        boolean useBlockGridDisplay = isAStarGrid(layoutResults);

        for(int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            if (useBlockGridDisplay) {
                CellState initialState = initialCellState(value);
                slotStates.put(i, initialState);

                if (initialState == CellState.START) startSlot = i;
                else if (initialState == CellState.GOAL) goalSlot = i;

                placeSupportLayer(pos);
                applyCellState(i, initialState);

                if (initialState == CellState.WALL) {
                    placeWallColumn(pos);
                }
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

    // Sinks the floor blocks by 1 into the ground
    protected void applyCellState(int slot, CellState state) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) return;
        if (state == CellState.DEFAULT) return;

        // Subtract 1 from Y to replace existing grass with path blocks
        Pos sunkenPos = layoutResults[slot].pos().add(0, -1, 0);
        Block newFloor = blockForState(state);
        placeWorldBlock(sunkenPos, newFloor);
    }

    // Places foundation underneath the sunken floor
    protected void placeSupportLayer(Pos basePos) {
        placeWorldBlock(basePos.add(0, -2, 0), Block.GRASS_BLOCK);
    }

    // Starts wall columns at floor level (Y-1)
    protected void placeWallColumn(Pos basePos) {
        Pos floorLevel = basePos.add(0, -1, 0);
        placeWorldBlock(floorLevel, Block.DARK_OAK_LEAVES);
        placeWorldBlock(floorLevel.add(0, 1, 0), Block.DARK_OAK_LEAVES);
        placeWorldBlock(floorLevel.add(0, 2, 0), Block.DARK_OAK_LEAVES);
        placeWorldBlock(floorLevel.add(0, 3, 0), Block.DARK_OAK_LEAVES);
        placeWorldBlock(floorLevel.add(0, 4, 0), Block.DARK_OAK_LEAVES);

    }

    protected Pos toEntityWalkPos(Pos cellPos) {
        return new Pos(cellPos.blockX() + 0.5, cellPos.blockY(), cellPos.blockZ() + 0.5);
    }

    public void moveVillager(int slot) {
        if (layoutResults == null || slot < 0 || slot >= layoutResults.length) return;
        Pos villagerPos = toEntityWalkPos(layoutResults[slot].pos());

        if (villagerEntity == null) {
            spawnVillagerAtPos(villagerPos);
        } else {
            villagerEntity.getNavigator().setPathTo(villagerPos);
        }
    }

    protected void spawnVillagerAtPos(Pos pos) {
        if (villagerEntity != null) villagerEntity.remove();
        villagerEntity = new EntityCreature(EntityType.VILLAGER);
        villagerEntity.setNoGravity(false);
        villagerEntity.setInstance(instance, pos);
    }


    protected void placeWorldBlock(Pos pos, Block block) {
        BlockPos blockPos = BlockPos.from(pos);
        ensureChunkLoaded(blockPos);
        overwrittenBlocks.putIfAbsent(blockPos, instance.getBlock(blockPos.x, blockPos.y, blockPos.z));
        instance.setBlock(blockPos.x, blockPos.y, blockPos.z, block);
    }

    protected void restoreWorldBlocks() {
        for (var entry : overwrittenBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            ensureChunkLoaded(pos);
            instance.setBlock(pos.x, pos.y, pos.z, entry.getValue());
        }
        overwrittenBlocks.clear();
    }

    protected void preloadChunks(LayoutResult[] layoutResults) {
        if (layoutResults == null || layoutResults.length == 0) return;
        Set<Long> loadedChunks = new HashSet<>();
        for (LayoutResult layoutResult : layoutResults) {
            Pos pos = layoutResult.pos();
            long key = (((long) pos.chunkX()) << 32) ^ (pos.chunkZ() & 0xffffffffL);
            if (loadedChunks.add(key)) instance.loadChunk(pos.chunkX(), pos.chunkZ()).join();
        }
    }

    protected void ensureChunkLoaded(BlockPos pos) {
        instance.loadChunk(Math.floorDiv(pos.x, 16), Math.floorDiv(pos.z, 16)).join();
    }

    protected static Block blockForState(CellState state) {
        return switch (state) {
            case DEFAULT -> Block.GRASS_BLOCK;
            case WALL -> Block.DARK_OAK_LEAVES;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.GREEN_CONCRETE;
            case PATH -> Block.ORANGE_CONCRETE;
        };
    }

    public void toggleCellState(int slot, CellState first, CellState second) {
        var current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        var next = current == first ? second : first;
        slotStates.put(slot, next);
        applyCellState(slot, next);
    }

    protected void spawnVillagerAtStart() {
        if (layoutResults == null) return;
        for (int i = 0; i < layoutResults.length; i++) {
            if (initialCellState(layoutResults[i].value()) == CellState.START) {
                moveVillager(i);
                break;
            }
        }
    }

    @Override
    public void cleanUp() {
        if (villagerEntity != null) { villagerEntity.remove(); villagerEntity = null; }
        restoreWorldBlocks();
        super.cleanUp();
    }

    protected static CellState initialCellState(Object value) {
        if (!(value instanceof Integer number)) return CellState.DEFAULT;
        return switch (number) {
            case 1 -> CellState.WALL;
            case 2 -> CellState.START;
            case 3 -> CellState.GOAL;
            default -> CellState.DEFAULT;
        };
    }

    protected boolean isAStarGrid(LayoutResult[] layoutResults) {
        if (layoutResults.length == 0) return false;
        for (var layoutResult : layoutResults) {
            if (!(layoutResult.value() instanceof Integer)) return false;
        }
        return true;
    }

    protected static int inferColumns(LayoutResult[] layoutResults) {
        if (layoutResults == null || layoutResults.length == 0) return 1;
        int firstRowZ = layoutResults[0].pos().blockZ();
        int columns = 0;
        for (LayoutResult layoutResult : layoutResults) {
            if (layoutResult.pos().blockZ() != firstRowZ) break;
            columns++;
        }
        return Math.max(columns, 1);
    }

    public Entity cameraTarget() { return villagerEntity; }

    protected record BlockPos(int x, int y, int z) {
        public static BlockPos from(Pos pos) { return new BlockPos(pos.blockX(), pos.blockY(), pos.blockZ()); }
    }
}