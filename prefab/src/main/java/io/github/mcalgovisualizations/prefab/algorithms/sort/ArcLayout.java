package io.github.mcalgovisualizations.prefab.algorithms.sort;

import io.github.mcalgovisualizations.prefab.Displays.EntityCreatureDisplay;
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

    private static final Random RANDOM = new Random();

    public ArcLayout() {
        this(6.0, 2.0, -Math.PI / 2, 3 * Math.PI / 2, false);
    }

    @Override
    public LayoutResult[] compute(List<Integer> model, Pos origin, Instance instance) {
        origin = origin.add(0, -2, 0);

        if (model == null || model.isEmpty()) {
            return new LayoutResult[0];
        }

        int size = model.size();
        double y = origin.y() + yOffset;

        if (size == 1) {
            double x = origin.x() + Math.cos(startAngle) * radius;
            double z = origin.z() + Math.sin(startAngle) * radius;

            return getEntities(
                    new Pos[]{new Pos(x, y, z)},
                    new int[]{model.getFirst()}
            );
        }

        int divisor = closed ? size : size - 1;
        double step = sweepAngle / divisor;

        Pos[] positions = new Pos[size];
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            double angle = startAngle + step * i;
            double x = origin.x() + Math.cos(angle) * radius;
            double z = origin.z() + Math.sin(angle) * radius;

            positions[i] = new Pos(x, y, z);
            values[i] = model.get(i);
        }

        return getEntities(positions, values);
    }

    private LayoutResult[] getEntities(Pos[] positions, int[] values) {
        if (positions.length != values.length) {
            throw new IllegalArgumentException("positions and values must be the same length!");
        }

        LayoutResult[] results = new LayoutResult[positions.length];

        int[] sorted = Arrays.stream(values)
                .sorted()
                .toArray();

        Map<Integer, Integer> rankByValue = new HashMap<>();
        for (int i = 0; i < sorted.length; i++) {
            rankByValue.putIfAbsent(sorted[i], i);
        }

        for (int i = 0; i < positions.length; i++) {
            int value = values[i];
            int rank = rankByValue.get(value);

            int entityIndex = values.length == 1
                    ? 0
                    : (int) Math.round(
                    rank * (SORTED_ENTITIES.length - 1.0) / (values.length - 1.0)
            );

            EntityType type = pickRandomNearbyEntity(entityIndex, values.length);

            results[i] = new LayoutResult(
                    value,
                    positions[i],
                    new EntityCreatureDisplay(
                            positions[i],
                            type,
                            Integer.toString(value)
                    )
            );
        }

        return results;
    }

    private EntityType pickRandomNearbyEntity(int entityIndex, int valueCount) {
        if (SORTED_ENTITIES.length == 0) {
            return EntityType.VILLAGER;
        }

        if (valueCount <= 1) {
            return SORTED_ENTITIES[0];
        }

        int bandSize = Math.max(2, SORTED_ENTITIES.length / valueCount);
        int halfBand = Math.max(1, bandSize / 2);

        int from = Math.max(0, entityIndex - halfBand);
        int to = Math.min(SORTED_ENTITIES.length - 1, entityIndex + halfBand);

        return SORTED_ENTITIES[from + RANDOM.nextInt(to - from + 1)];
    }

    private static final Set<String> BLACKLIST = new HashSet<>(Stream.of(
            "breeze", // too noisy
            "phantom", // when looking at a pos, it looks downwards (assumes it's flying i think)
            "wither", // too noisy visually and audio
            "warden", // too noisy
            "pufferfish", // the height assumes is already puffed pufferfish
            "shulker" // already use shulkers for Selection Sort
    ).map(s -> s.toLowerCase().replace("_", "")).toList());

    private static final EntityType[] SORTED_ENTITIES = Stream.of(
                    "allay",
                    "armadillo",
                    "axolotl",
                    "bat",
                    "bee",
                    "bogged",
                    "breeze",
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
                    "phantom",
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
                    "warden",
                    "witch",
                    "wither",
                    "wither_skeleton",
                    "wolf",
                    "zoglin",
                    "zombie",
                    "zombie_horse",
                    "zombie_nautilus",
                    "zombie_villager",
                    "zombified_piglin"
            )
            .filter(ArcLayout::isAllowedEntity)
            .map(EntityType::fromKey)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(EntityType::height))
            .toArray(EntityType[]::new);

    private static boolean isAllowedEntity(String entityName) {
        return !BLACKLIST.contains(entityName.toLowerCase());
    }
}