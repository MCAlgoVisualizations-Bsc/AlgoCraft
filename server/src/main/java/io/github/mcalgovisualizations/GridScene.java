package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.visualization.renderer.IBlockStateDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.events.CellState;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.Displays.MobDisplay;

import net.minestom.server.instance.block.Block;

import java.util.*;

/**
 * Scene = Minestom world state.
 *
 */
public class GridScene extends AbstractScene {
    // Visual state
    private final Map<Integer, CellState> slotStates = new HashMap<>();

    public GridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        cleanUp();
        boolean useBlockGridDisplay = isAStarGrid(layoutResults);

        for(int i = 0; i < layoutResults.length; i++) {
            var pos = layoutResults[i].pos();
            var value = layoutResults[i].value();

            IDisplayValue dv;
            if (useBlockGridDisplay) {
                CellState initialState = initialCellState(value);
                Block initialBlock = blockForState(initialState);
                // Maze cells are pure block visuals; hide numeric labels.
                dv = new BlockDisplay(pos, initialBlock, "maze", false);
                slotStates.put(i, initialState);
            } else {
                dv = new MobDisplay(pos, value.toString());
            }

            displaysBySlot.put(i, dv);
            dv.setInstance(instance);
        }
    }

    public void toggleCellState(int slot, CellState first, CellState second) {
        var current = slotStates.getOrDefault(slot, CellState.DEFAULT);
        var next = current == first ? second : first;
        slotStates.put(slot, next);
        applyCellState(slot, next);
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
            case WALL -> Block.BLACK_CONCRETE;
            case START -> Block.LIME_CONCRETE;
            case GOAL -> Block.RED_CONCRETE;
            case OPEN -> Block.LIGHT_BLUE_CONCRETE;
            case CLOSED -> Block.GREEN_CONCRETE;
            case PATH -> Block.ORANGE_CONCRETE;
        };
    }

    private boolean isAStarGrid(LayoutResult[] layoutResults) {
        if (layoutResults.length == 0) return false;
        for (var layoutResult : layoutResults) {
            Object raw = layoutResult.value();
            if (!(raw instanceof Integer number)) return false;
            if (number < 0 || number > 3) return false;
        }
        return true;
    }
}
