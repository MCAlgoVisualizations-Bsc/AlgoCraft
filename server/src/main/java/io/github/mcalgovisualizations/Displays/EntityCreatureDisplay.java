package io.github.mcalgovisualizations.Displays;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;

import java.util.concurrent.CompletableFuture;

public class EntityCreatureDisplay implements IDisplayValue {
    private Pos initialPos;
    private final EntityCreature entity;
    private final Entity textEntity;

    public EntityCreatureDisplay(Pos pos, EntityType entityType, String displayText) {
        this(pos, entityType, displayText, false);
    }

    public EntityCreatureDisplay(Pos pos, EntityType entityType, String displayText, boolean setNoGravity) {
        this.initialPos = pos;
        this.entity = new EntityCreature(entityType);
        this.entity.setNoGravity(setNoGravity);
        
        // Increase movement speed for better visualization
        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35f);

        this.textEntity = new Entity(EntityType.TEXT_DISPLAY);
        setupText(displayText);
    }

    public void setNoGravity(boolean setNoGravity) {
        entity.setNoGravity(setNoGravity);
    }

    @Override
    public Pos getPos() {
        return this.entity.isActive() ? this.entity.getPosition() : initialPos;
    }

    @Override
    public void setInstance(Instance instance) {
        Pos spawnPos = getPos();
        entity.setInstance(instance, spawnPos).thenRun(() -> {
            if (textEntity.isRemoved()) return;
            textEntity.setInstance(instance, spawnPos).thenRun(() -> {
                if (entity.isRemoved()) return;
                entity.addPassenger(textEntity);
            });
        });
    }

    @Override
    public void teleport(Pos pos) {
        if (entity.isActive()) {
            entity.teleport(pos);
        } else {
            this.initialPos = pos;
        }
    }

    @Override
    public CompletableFuture<Void> walkTo(Pos pos) {
        if (entity.isActive()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            // Use speed 1.0 (relative to entity's speed attribute)
            entity.getNavigator().setPathTo(pos, 1.0, () -> future.complete(null));
            return future;
        } else {
            this.initialPos = pos;
            return CompletableFuture.completedFuture(null);
        }
    }

    public double getEyeHeight() {
        return entity.getEyeHeight();
    }

    @Override
    public void addViewer(Player player) {
        entity.addViewer(player);
        textEntity.addViewer(player);
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
        // Offset the text display upwards using translation instead of manual teleportation
        meta.setTranslation(new Vec(0, entity.getEyeHeight() + 0.5, 0));
    }
}
