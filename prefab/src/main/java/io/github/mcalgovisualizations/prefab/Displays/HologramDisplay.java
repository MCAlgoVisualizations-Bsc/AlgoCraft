package io.github.mcalgovisualizations.prefab.Displays;

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
 * Floating text hologram displayed above the visualization.
 */
@Deprecated
public final class HologramDisplay implements IDisplayValue {
    private final Pos pos;
    private final Entity textEntity;

    /**
     * Creates a new hologram at the given position.
     */
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
    /**
     * Updates the displayed text.
     */
    public void setText(Component text) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(text);
    }

    @Override
    /**
     * Returns the hologram position.
     */
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

    /**
     * Removes the hologram entity.
     */
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




