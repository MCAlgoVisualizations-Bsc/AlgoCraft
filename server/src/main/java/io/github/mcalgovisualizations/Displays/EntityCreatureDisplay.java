package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

import java.util.concurrent.CompletableFuture;

public class EntityCreatureDisplay implements IDisplayValue {
    private Pos pos;
    private final EntityCreature entity;
    private final Entity textEntity;

    public EntityCreatureDisplay(Pos pos, EntityType entityType, String displayText) {
        this.pos = pos;
        this.entity = new EntityCreature(entityType);
        this.entity.setNoGravity(true);

        this.textEntity = new Entity(EntityType.TEXT_DISPLAY);
        setupText(displayText);
    }

    @Override
    public Pos getPos() {
        return this.pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    @Override
    public void setValue(int value) { /* legacy */ }

    @Override
    public void setInstance(Instance instance) {
        entity.setInstance(instance, pos);
        textEntity.setInstance(instance, pos.add(0, 2,0));
    }

    @Override
    public void addViewer(Player player) {
        entity.addViewer(player);
    }

    public void lookAt(Pos pos) {
        entity.lookAt(pos);
    }

    @Override
    public void remove() {
        entity.remove();
        textEntity.remove();
    }

    @Override
    public void teleport(Pos pos) {
        entity.teleport(pos);
        textEntity.teleport(pos.add(0,2,0));
        setPos(pos);
    }

    @Override
    public void setGlowing(boolean highlighted) {
        entity.setGlowing(highlighted);
    }

    @Override
    public boolean isSpawned() {
        return entity.isActive();
    }

    private void setupText(String text) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(text, NamedTextColor.GOLD));
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setScale(new Vec(4));
        meta.setHasNoGravity(true);
        meta.setPosRotInterpolationDuration(5);
        meta.setTransformationInterpolationStartDelta(0);
    }
}
