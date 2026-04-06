package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.IStylingProfile;
import io.github.mcalgovisualizations.Displays.AbstractParticleDisplay;
import io.github.mcalgovisualizations.Displays.BlockDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

import java.util.List;

public final class ParticleTreeNodeStylingProfile implements IStylingProfile {
    private final BstNodeStylingProfile.NodeRole role;
    private final Pos[] targets;

    public ParticleTreeNodeStylingProfile(BstNodeStylingProfile.NodeRole role, Pos... targets) {
        this.role = role;
        this.targets = targets == null ? new Pos[0] : targets;
    }

    @Override
    public IDisplayValue applyStyle(String value, Pos pos) {
        Block block = switch (role) {
            case ROOT -> Block.OAK_LOG;
            case LEAF -> Block.OAK_LEAVES;
            case INTERNAL -> Block.OAK_PLANKS;
        };
        return new ParticleTreeNodeDisplay(new BlockDisplay(pos, block, value), targets);
    }

    private static final class ParticleTreeNodeDisplay extends AbstractParticleDisplay {
        private final Pos[] targets;

        private ParticleTreeNodeDisplay(BlockDisplay base, Pos[] targets) {
            super(base);
            this.targets = targets;
        }

        @Override
        protected List<Pos> targets() {
            return nonNullTargets(targets);
        }
    }
}

