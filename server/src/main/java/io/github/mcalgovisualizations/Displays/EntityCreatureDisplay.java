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
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class EntityCreatureDisplay implements IDisplayValue {
    private Pos pos;
    private final EntityCreature entity;
    private final Entity textEntity;
    private boolean gravity;

    public EntityCreatureDisplay(Pos pos, EntityType entityType, String displayText) {
        this(pos, entityType, displayText, false);
    }

    public EntityCreatureDisplay(Pos pos, EntityType entityType, String displayText, boolean setNoGravity) {
        this.pos = pos;
        this.entity = new EntityCreature(entityType);
        this.gravity = setNoGravity;
        this.entity.setNoGravity(setNoGravity);

        this.textEntity = new Entity(EntityType.TEXT_DISPLAY);
        setupText(displayText);

        this.entity.eventNode().addListener(EntityEvent.class, e -> {
           if(entity.equals(e.getEntity()))
               System.out.println("Entity Event " + entityType.name());
        });
    }

    public void setNoGravity(boolean setNoGravity) {
        entity.setNoGravity(setNoGravity);
    }

    @Override
    public Pos getPos() {
        return this.pos;
    }

    @Override
    public void setInstance(Instance instance) {
        entity.setInstance(instance, pos);
        textEntity.setInstance(instance, getTextOffset(entity.getPosition()));
    }

    @Override
    public void setInstance(Instance instance, Pos pos) {
        entity.setInstance(instance, pos);
        textEntity.setInstance(instance, getTextOffset(entity.getPosition()));
    }

    @Override
    public void teleport(Pos pos) {
        this.pos = pos;
        entity.teleport(pos);
        textEntity.teleport(getTextOffset(entity.getPosition()));
    }

    private Pos getTextOffset(Pos pos) {
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

    public void lookAt(EntityCreatureDisplay o) {
        entity.lookAt(o.entity);
    }

    @Override
    public void remove() {
        entity.remove();
        textEntity.remove();
    }

    public void jump() {
        if(!entity.isOnGround()) {
            System.err.println(entity.getEntityType().name() + " is not on ground!");
            return;
        }
        this.entity.teleport(this.entity.getPosition().add(0, 1, 0));
    }

    public void goTo(Pos pos) {
        entity.getNavigator().setPathTo(pos);
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

    public void setTeam(Team team) {
        entity.setTeam(team);
    }

    public void clearTeam() {
        entity.setTeam(null);
    }
}
