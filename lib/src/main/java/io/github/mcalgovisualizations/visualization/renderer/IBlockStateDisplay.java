package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.instance.block.Block;

/**
 * Display contract for visuals that can change their block state.
 */
public interface IBlockStateDisplay extends IDisplayValue {
    /**
     * Updates the underlying block state shown by the display.
     *
     * @param block the new block state
     */
    void setBlock(Block block);
}

