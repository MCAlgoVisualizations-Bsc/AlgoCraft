package io.github.mcalgovisualizations.Displays;


import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

import java.util.concurrent.CompletableFuture;

public class NodeDisplay implements IDisplayValue {
    private final Entity textEntity;
    private Pos pos;
    private static final double FONT_BASELINE_CORRECTION = -0.25;

    public NodeDisplay(String value, Pos pos) {
        textEntity = new Entity(EntityType.TEXT_DISPLAY);
        this.pos = translatePos(pos);
        setupText(value);
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    @Override
    public void setInstance(Instance instance) {
        textEntity.setInstance(instance, pos);
    }

    @Override
    public void setInstance(Instance instance, Pos pos) {
        this.pos = pos;
        textEntity.setInstance(instance, pos);
    }

    @Override
    public void addViewer(Player player) {
        textEntity.addViewer(player);
    }

    @Override
    public void remove() {
        textEntity.remove();
    }

    @Override
    public void teleport(Pos pos) {
        this.pos = translatePos(pos);
        textEntity.teleport(this.pos);
    }


    @Override
    public void setGlowing(boolean highlighted) {
        textEntity.setGlowing(highlighted);
    }

    @Override
    public boolean isSpawned() {
        return textEntity.isActive();
    }

    private void setupText(String text) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(text, NamedTextColor.GOLD));
        meta.setAlignment(TextDisplayMeta.Alignment.CENTER);
        meta.setBackgroundColor(0);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.FIXED);
        meta.setScale(new Vec(2));
        meta.setHasNoGravity(true);
        meta.setPosRotInterpolationDuration(5);
        meta.setTransformationInterpolationStartDelta(0);
        meta.setTranslation(new Vec(0, FONT_BASELINE_CORRECTION, 0));
    }

    private Pos translatePos(Pos pos) {
        return new Pos(pos.blockX() + 0.5,pos.blockY() + 1.01,pos.blockZ() + 0.5)
                .withYaw(180f)
                .withPitch(-90);
    }
}
