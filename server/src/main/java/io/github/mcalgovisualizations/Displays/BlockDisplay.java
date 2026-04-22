package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IBlockStateDisplay;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class BlockDisplay implements IBlockStateDisplay {
    private static final double TEXT_Y_OFFSET = 2.0;

    public final Entity blockEntity;
    private final Entity textEntity;

    private Pos pos;

    public BlockDisplay(Instance instance, Pos pos, Block block, String text) {
        this(instance, pos, block, text, true);
    }

    public BlockDisplay(Instance instance, Pos pos, Block block, String text, boolean showLabel) {
        if (block == null) throw new NullPointerException("block cannot be null");
        if (instance == null) throw new NullPointerException("instance cannot be null");
        if (showLabel && (text == null || text.isBlank())) throw new IllegalArgumentException("text cannot be blank");

        this.blockEntity = new Entity(EntityType.BLOCK_DISPLAY);
        this.textEntity = showLabel ? new Entity(EntityType.TEXT_DISPLAY) : null;
        this.pos = pos;

        setupBlock(block);
        if (textEntity != null) {
            setupText(text);
        }


    }

    public BlockDisplay(Pos pos, Block block, String text) {
        this(pos, block, text, true);
    }

    public BlockDisplay(Pos pos, Block block, String text, boolean showLabel) {
        if (block == null) throw new NullPointerException("block cannot be null");
        if (showLabel && (text == null || text.isBlank())) throw new IllegalArgumentException("text cannot be blank");

        this.blockEntity = new Entity(EntityType.BLOCK_DISPLAY);
        this.textEntity = showLabel ? new Entity(EntityType.TEXT_DISPLAY) : null;
        this.pos = pos;

        setupBlock(block);
        if (textEntity != null) {
            setupText(text);
        }
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

    private void setupText(String text) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(text, NamedTextColor.GOLD));
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setScale(new Vec(2.5));
        meta.setHasNoGravity(true);
        meta.setPosRotInterpolationDuration(5);
        meta.setTransformationInterpolationStartDelta(0);
    }


    @Override
    public void setInstance(Instance instance) {
        blockEntity.setInstance(instance, pos);
        if (textEntity != null) {
            textEntity.setInstance(instance, pos.add(0, TEXT_Y_OFFSET, 0));
        }
    }

    @Override
    public void addViewer(Player player) {
        this.blockEntity.addViewer(player);
        if (textEntity != null) {
            this.textEntity.addViewer(player);
        }
    }

    public void remove() {
        this.blockEntity.remove();
        if (textEntity != null) {
            this.textEntity.remove();
        }
    }

    public void teleport(Pos pos) {
        this.pos = pos;
        this.blockEntity.teleport(pos);
        if (textEntity != null) {
            this.textEntity.teleport(pos.add(0, TEXT_Y_OFFSET, 0));
        }
    }

    public void setValue(int value) {
        setText(Integer.toString(value));
    }

    public void setText(String text) {
        if (textEntity == null) return;
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(text, NamedTextColor.GOLD));
    }

    public void setGlowing(boolean highlighted) {
        blockEntity.setGlowing(highlighted);
        if (textEntity != null) {
            textEntity.setGlowing(highlighted);
        }
    }


    public void setBlock(Block block) {
        if (block == null) return;
        var meta = (BlockDisplayMeta) blockEntity.getEntityMeta();
        meta.setBlockState(block);
    }

    public boolean isSpawned() {
        return blockEntity.isActive() || (textEntity != null && textEntity.isActive());
    }
}
