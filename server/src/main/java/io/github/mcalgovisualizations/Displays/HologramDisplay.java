package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

/**
 * A floating text hologram displayed above the visualization.
 * Uses a TEXT_DISPLAY entity so it's always billboard-facing the viewer.
 */
public final class HologramDisplay implements IDisplayValue {
    private final Pos pos;
    private final Entity textEntity;

    public HologramDisplay(String value, Pos pos) {
        this.pos = pos;
        this.textEntity = new Entity(EntityType.TEXT_DISPLAY);

        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(value));
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setHasNoGravity(true);
        meta.setScale(new Vec(3));
        meta.setShadow(true);
        meta.setBackgroundColor(0x40000000); // semi-transparent dark background
    }

    /** Update the displayed text. */
    public void setText(Component text) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(text);
    }

    @Override
    public Pos getPos() {
        return this.pos;
    }

    @Override
    public void setInstance(Instance instance) {
        textEntity.setInstance(instance, pos);
    }

    @Override
    public void setInstance(Instance instance, Pos pos) {
        textEntity.setInstance(instance, pos);
    }

    public void addViewer(Player player) {
        textEntity.addViewer(player);
    }

    public void remove() {
        textEntity.remove();
    }

    @Override
    public void teleport(Pos pos) {
        this.textEntity.teleport(pos);
    }

    @Override
    public void setGlowing(boolean highlighted) {
        this.textEntity.setGlowing(highlighted);
    }

    @Override
    public boolean isSpawned() {
        return this.textEntity.isActive();
    }
}




