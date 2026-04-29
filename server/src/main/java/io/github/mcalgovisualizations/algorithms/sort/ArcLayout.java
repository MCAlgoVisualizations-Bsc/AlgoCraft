package io.github.mcalgovisualizations.algorithms.sort;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;

import java.util.*;
import java.util.stream.Stream;

public record ArcLayout(
        double radius,
        double yOffset,
        double startAngle,
        double sweepAngle,
        boolean closed
) implements ILayout<List<Integer>> {

    public ArcLayout() {
        this(6.0, 2.0, -Math.PI / 2, 3 * Math.PI / 2, false);
    }

    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }
        int size = model.size();
        double y = origin.y() + yOffset;

        if (size == 1) {
            double x = origin.x() + Math.cos(startAngle) * radius;
            double z = origin.z() + Math.sin(startAngle) * radius;
            Pos pos = new Pos(x, y, z);
            return getEntities(new Pos[] {pos}, new int[] {model.getFirst()});
        }

        int divisor = closed ? size : size - 1;
        double step = sweepAngle / divisor;

        Pos[] pos = new Pos[size];
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            double angle = startAngle + step * i;
            double x = origin.x() + Math.cos(angle) * radius;
            double z = origin.z() + Math.sin(angle) * radius;

            pos[i] = new Pos(x, y, z);
            values[i] = model.get(i);
        }

        return getEntities(pos, values);
    }

    private LayoutResult[] getEntities(Pos[] pos, int[] values) {
        if (pos.length != values.length) {
            throw new IllegalArgumentException("pos and values must be the same length!");
        }

        LayoutResult[] entities = new LayoutResult[pos.length];

        int[] sorted = Arrays.stream(values)
                .sorted()
                .toArray();

        Map<Integer, Integer> rankByValue = new HashMap<>();
        for (int i = 0; i < sorted.length; i++) {
            rankByValue.putIfAbsent(sorted[i], i);
        }

        for (int i = 0; i < pos.length; i++) {
            int value = values[i];
            int rank = rankByValue.get(value);

            int entityIndex;

            if (values.length == 1) {
                entityIndex = 0;
            } else {
                entityIndex = (int) Math.round(
                        rank * (sortedEntities.length - 1.0) / (values.length - 1.0)
                );
            }

            EntityType type = sortedEntities.length == 0
                    ? EntityType.VILLAGER
                    : sortedEntities[entityIndex];

            entities[i] = new LayoutResult(
                    value,
                    pos[i],
                    new EntityCreatureDisplay(pos[i], type, Integer.toString(value))
            );
        }

        return entities;
    }

    /**
     * Sorted by height.
     */
    private static final EntityType[] sortedEntities = Stream.of(
                    "allay",
                    "armadillo",
                    "axolotl",
                    "bat",
                    "bee",
                    "bogged",
                    "camel",
                    "camel_husk",
                    "cat",
                    "cave_spider",
                    "chicken",
                    "cod",
                    "copper_golem",
                    "cow",
                    "creaking",
                    "creeper",
                    "dolphin",
                    "donkey",
                    "drowned",
                    "elder_guardian",
                    "enderman",
                    "endermite",
                    "evoker",
                    "fox",
                    "frog",
                    "glow_squid",
                    "goat",
                    "guardian",
                    "hoglin",
                    "horse",
                    "husk",
                    "illusioner",
                    "iron_golem",
                    "llama",
                    "magma_cube",
                    "mooshroom",
                    "mule",
                    "ocelot",
                    "panda",
                    "parched",
                    "parrot",
                    //"phantom",
                    "pig",
                    "piglin",
                    "piglin_brute",
                    "pillager",
                    "polar_bear",
                    "pufferfish",
                    "rabbit",
                    "ravager",
                    "salmon",
                    "sheep",
                    "shulker",
                    "silverfish",
                    "skeleton",
                    "skeleton_horse",
                    "slime",
                    "sniffer",
                    "snow_golem",
                    "spider",
                    "squid",
                    "stray",
                    "strider",
                    "tadpole",
                    "trader_llama",
                    "tropical_fish",
                    "turtle",
                    "vex",
                    "villager",
                    "vindicator",
                    "wandering_trader",
                    "witch",
                    "wither_skeleton",
                    "wolf",
                    "zoglin",
                    "zombie",
                    "zombie_horse",
                    "zombie_nautilus",
                    "zombie_villager",
                    "zombified_piglin"
            )
            .map(EntityType::fromKey)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(EntityType::height)) // sort by height
            .toArray(EntityType[]::new);
}
