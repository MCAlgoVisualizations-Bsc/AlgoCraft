package io.github.mcalgovisualizations.visualization.renderer.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IBlockStateDisplay;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class BlockDisplay implements IBlockStateDisplay {
    public final Instance instance;
    public final Entity blockEntity;

    private Pos pos;

    public BlockDisplay(Instance instance, Pos pos, Block block, String text) {
        if (block == null) throw new NullPointerException("block cannot be null");
        if (instance == null) throw new NullPointerException("instance cannot be null");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text cannot be blank");

        this.instance = instance;

        this.blockEntity = new Entity(EntityType.BLOCK_DISPLAY);

        this.pos = pos;

        setupBlock(block);
    }

    public Pos getPos() {
        return pos;
    }

    private void setupBlock(Block block) {
        var meta = (BlockDisplayMeta) blockEntity.getEntityMeta();

        meta.setBlockState(block);
        meta.setHasNoGravity(true);
        meta.setPosRotInterpolationDuration(0);
        meta.setTransformationInterpolationStartDelta(0);
    }


    @Override
    public void setInstance(Instance instance) {
        blockEntity.setInstance(instance, pos);
    }

    @Override
    public void addViewer(Player player) {
        this.blockEntity.addViewer(player);
    }

    public void remove() {
        this.blockEntity.remove();
    }

    public void teleport(Pos pos) {
        this.pos = pos;
        this.blockEntity.teleport(pos);
    }

    public void setValue(int value) {
        // Numeric overlays are intentionally disabled for grid visualizations.
    }

    public void setGlowing(boolean highlighted) {
        blockEntity.setGlowing(highlighted);
    }


    public void setBlock(Block block) {
        if (block == null) return;
        var meta = (BlockDisplayMeta) blockEntity.getEntityMeta();
        meta.setBlockState(block);
    }

    public boolean isSpawned() {
        return blockEntity.isActive();
    }
}
