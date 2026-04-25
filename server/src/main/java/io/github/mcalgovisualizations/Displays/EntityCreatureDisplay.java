package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;

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

    @Override
    public void setInstance(Instance instance) {
        entity.setInstance(instance, pos);
        textEntity.setInstance(instance, getTextOffset());
    }

    @Override
    public void teleport(Pos pos) {
        this.pos = pos;
        entity.teleport(pos);
        textEntity.teleport(getTextOffset());
    }

    private Pos getTextOffset() {
        return pos.add(0, entity.getEyeHeight() + 1, 0);
    }

    public double getEyeHeight() {
        return entity.getEyeHeight();
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


    public void kill() {
        entity.kill();
        textEntity.remove();
    }

    @Override
    public void setGlowing(boolean highlighted) {
        entity.setGlowing(highlighted);
    }

    public void shakeHead() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            var pos = entity.getPosition();
            entity.teleport(pos.withYaw(pos.yaw() + 25f));
        }).delay(2, TimeUnit.SERVER_TICK).schedule();
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
