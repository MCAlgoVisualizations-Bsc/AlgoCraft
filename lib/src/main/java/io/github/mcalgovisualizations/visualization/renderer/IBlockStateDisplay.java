package io.github.mcalgovisualizations.visualization.renderer.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.instance.block.Block;

public interface IBlockStateDisplay extends IDisplayValue {
    void setBlock(Block block);
}

