package io.github.mcalgovisualizations.prefab.Displays;

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
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.PacketSendingUtils;
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

        this.entity.eventNode().addListener(EntityEvent.class, e -> {
           if(entity.equals(e.getEntity()))
               System.out.println("Entity Event " + entityType.name());
        });
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
    public void setInstance(Instance instance, Pos pos) {
        this.initialPos = pos;
        setInstance(instance);
    }

    @Override
    public void teleport(Pos pos) {
        if (entity.isActive()) {
            entity.teleport(pos);
        } else {
            this.initialPos = pos;
        }
    }

    /**
     * Requests the display to move to a new position, potentially using pathfinding.
     *
     * @param pos the target position
     * @return a future that completes when the target is reached
     */
    public CompletableFuture<Void> walkTo(Pos pos) {
        if (!entity.isActive()) {
            this.initialPos = pos;
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        entity.getNavigator().setPathTo(pos, 1.0, () -> {
            if (!future.isDone()) {
                future.complete(null);
            }
        });

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (future.isDone()) {
                return;
            }

            /*
             * Fallback:
             * pathfinding failed, got stuck, or callback never fired.
             * Teleport so the animation pipeline can continue.
             */
            entity.teleport(pos);
            future.complete(null);
        }).delay(60, TimeUnit.SERVER_TICK).schedule();

        return future;
    }

    private Pos getTextOffset(Pos pos) {
        return pos.add(0, entity.getEyeHeight() + 1, 0);
    }

    public double getEyeHeight() {
        return entity.getEyeHeight();
    }

    public EntityCreature getEntity() {
        return entity;
    }

    @Override
    public void addViewer(Player player) {
        entity.addViewer(player);
        textEntity.addViewer(player);
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

    public void spawnParticleAura(Particle particle) {
        Instance instance = entity.getInstance();
        if (instance == null) return;

        var center = entity.getPosition().add(0, entity.getEyeHeight() * 0.5, 0);

        var packet = new ParticlePacket(
                particle,
                center,
                new Vec(0.4, 0.6, 0.4), // random spread around entity
                0.01f,                  // particle speed
                20                      // amount
        );

        PacketSendingUtils.sendGroupedPacket(instance.getPlayers(), packet);
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

    public void setTeam(Team team) {
        entity.setTeam(team);
    }

    public void clearTeam() {
        entity.setTeam(null);
    }
}
