package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.instance.block.Block;

public interface IBlockStateDisplay extends IDisplayValue {
    void setBlock(Block block);
}

