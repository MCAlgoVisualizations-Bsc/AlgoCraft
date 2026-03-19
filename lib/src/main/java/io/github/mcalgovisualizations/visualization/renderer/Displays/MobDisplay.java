package io.github.mcalgovisualizations.visualization.renderer.Displays;

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

/**
 * Represents one sorting element as a living mob whose type scales with value (1–10).
 *
 *  1  → CHICKEN    (~0.7 m)
 *  2  → RABBIT     (~0.5 m)
 *  3  → CAT        (~0.7 m)
 *  4  → FOX        (~0.7 m)
 *  5  → PIG        (~0.9 m)
 *  6  → COW        (~1.4 m)
 *  7  → HORSE      (~1.6 m)
 *  8  → CAMEL      (~2.4 m)
 *  9  → IRON_GOLEM (~2.7 m)
 * 10  → WARDEN     (~2.9 m)
 */
public class MobDisplay implements IDisplayValue {

    private static final EntityType[] MOB_LADDER = {
            EntityType.CHICKEN,    // 1
            EntityType.RABBIT,     // 2
            EntityType.CAT,        // 3
            EntityType.FOX,        // 4
            EntityType.PIG,        // 5
            EntityType.COW,        // 6
            EntityType.HORSE,      // 7
            EntityType.CAMEL,      // 8
            EntityType.POLAR_BEAR, // 9
            EntityType.IRON_GOLEM, // 10
    };

    private static final double TEXT_Y_OFFSET = 3.5;

    private final Instance instance;
    private final EntityCreature mobEntity;
    private final Entity textEntity;
    private Pos pos;

    public MobDisplay(Instance instance, Pos pos, int value, String text) {
        if (instance == null) throw new NullPointerException("instance cannot be null");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text cannot be blank");

        this.instance = instance;
        this.pos = pos;

        this.mobEntity = new EntityCreature(mobTypeForValue(value));
        this.mobEntity.setNoGravity(true);
        this.textEntity = new Entity(EntityType.TEXT_DISPLAY);
        setupText(text);
    }

    public static EntityType mobTypeForValue(int value) {
        int index = Math.clamp(value - 1, 0, MOB_LADDER.length - 1);
        return MOB_LADDER[index];
    }

    public Pos getPos() {
        return pos;
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

    @Override
    public void setInstance() {
        mobEntity.setInstance(instance, pos);
        textEntity.setInstance(instance, pos.add(0, TEXT_Y_OFFSET, 0));
    }

    @Override
    public void addViewer(Player player) {
        mobEntity.addViewer(player);
        textEntity.addViewer(player);
        mobEntity.lookAt(player);
    }

    @Override
    public void remove() {
        mobEntity.remove();
        textEntity.remove();
    }

    @Override
    public void teleport(Pos pos) {
        this.pos = pos;
        mobEntity.teleport(pos);
        textEntity.teleport(pos.add(0, TEXT_Y_OFFSET, 0));
    }

    @Override
    public void setGlowing(boolean highlighted) {
        mobEntity.setGlowing(highlighted);
        textEntity.setGlowing(highlighted);
    }

    @Override
    public boolean isSpawned() {
        return mobEntity.isActive() || textEntity.isActive();
    }

    public void setValue(int value) {
        var meta = (TextDisplayMeta) textEntity.getEntityMeta();
        meta.setText(Component.text(Integer.toString(value), NamedTextColor.GOLD));
    }
}
