package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.ui.IStylingProfile;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public final class BstNodeStylingProfile implements IStylingProfile {

    public enum NodeRole {
        ROOT,
        INTERNAL,
        LEAF
    }

    private final NodeRole role;

    public BstNodeStylingProfile(NodeRole role) {
        this.role = role;
    }

    @Override
    public IDisplayValue applyStyle(String value, Pos pos) {
        Block block = switch (role) {
            case ROOT -> Block.OAK_LOG;
            case LEAF -> Block.OAK_LEAVES;
            case INTERNAL -> Block.OAK_PLANKS;
        };
        return new BlockDisplay(pos, block, value);
    }
}

