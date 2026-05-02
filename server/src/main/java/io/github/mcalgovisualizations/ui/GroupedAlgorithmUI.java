package io.github.mcalgovisualizations.ui;

import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import io.github.mcalgovisualizations.visualization.ui.IAlgorithmUI;
import io.github.mcalgovisualizations.visualization.ui.Tags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class GroupedAlgorithmUI implements IAlgorithmUI {
    private static final int MAX_SELECTOR_ITEMS = 54;

    private static final List<String> PREFERRED_ORDER = List.of(
            "insertion sort (ints)", "small insertion sort (ints)", "insertion sort (string)",
            "sorted insertion", "bst search", "unordered_tree_search", "tst search",
            "a* pathfinding (4-way)", "bfs pathfinding (4-way)", "dfs pathfinding (4-way)",
            "greedy best-first (4-way)", "max flow (edmonds-karp)"
    );

    private final AlgorithmUI delegate = new AlgorithmUI();

    @Override
    public Inventory openSelector(Set<String> algorithms, Function<String, AlgorithmPresentation> presentationResolver) {
        if (algorithms.size() > MAX_SELECTOR_ITEMS) {
            throw new IllegalArgumentException("Too many algorithms for selector");
        }

        int rowCount = Math.max(1, (int) Math.ceil(algorithms.size() / 9.0));
        InventoryType inventoryType = inventoryTypeForRows(rowCount);
        Inventory inventory = new Inventory(inventoryType, Component.text("Select Algorithm", NamedTextColor.DARK_PURPLE));

        List<String> orderedAlgorithms = orderedAlgorithms(algorithms);
        for (int i = 0; i < orderedAlgorithms.size(); i++) {
            String algorithm = orderedAlgorithms.get(i);
            AlgorithmPresentation presentation = presentationResolver.apply(algorithm);
            if (presentation == null) presentation = new AlgorithmPresentation(algorithm);

            ItemStack item = ItemStack.builder(presentation.icon())
                    .customName(presentation.getCustomName())
                    .lore(presentation.getComponents())
                    .set(Tags.ALGO_ID_TAG, algorithm)
                    .build();

            inventory.setItemStack(i, item);
        }
        return inventory;
    }

    @Override
    public void applyRunningLayout(Player player) {
        delegate.applyRunningLayout(player);
    }

    @Override
    public void applyDefaultLayout(Player player) {
        delegate.applyDefaultLayout(player);
    }

    private static InventoryType inventoryTypeForRows(int rows) {
        return switch (Math.clamp(rows, 1, 6)) {
            case 1 -> InventoryType.CHEST_1_ROW;
            case 2 -> InventoryType.CHEST_2_ROW;
            case 3 -> InventoryType.CHEST_3_ROW;
            case 4 -> InventoryType.CHEST_4_ROW;
            case 5 -> InventoryType.CHEST_5_ROW;
            default -> InventoryType.CHEST_6_ROW;
        };
    }

    private static List<String> orderedAlgorithms(Set<String> algorithms) {
        List<String> ordered = new ArrayList<>(algorithms.size());
        Set<String> seen = new HashSet<>();
        for (String id : PREFERRED_ORDER) {
            if (algorithms.contains(id)) {
                ordered.add(id);
                seen.add(id);
            }
        }
        algorithms.stream()
                .filter(id -> !seen.contains(id))
                .sorted(Comparator.naturalOrder())
                .forEach(ordered::add);
        return ordered;
    }
}