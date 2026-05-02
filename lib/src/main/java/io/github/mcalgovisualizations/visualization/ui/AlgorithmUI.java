package io.github.mcalgovisualizations.visualization.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static io.github.mcalgovisualizations.visualization.ui.Tags.*;

public class AlgorithmUI implements IAlgorithmUI {
    private static final int MAX_SELECTOR_ITEMS = 54;

    @Override
    public Inventory openSelector(Set<String> algorithms, Function<String, AlgorithmPresentation> presentationResolver) {
        if (algorithms.size() > MAX_SELECTOR_ITEMS) throw new IllegalArgumentException("Too many algorithms");

        int rowCount = Math.max(1, (int) Math.ceil(algorithms.size() / 9.0));
        InventoryType inventoryType = inventoryTypeForRows(rowCount);
        List<Integer> slots = centeredSlots(rowCount, algorithms.size());

        Inventory inventory = new Inventory(inventoryType, Component.text("Select Algorithm", NamedTextColor.DARK_PURPLE));
        List<String> sortedAlgorithms = algorithms.stream().sorted(Comparator.naturalOrder()).toList();

        int i = 0;
        for (String algorithm : sortedAlgorithms) {
            AlgorithmPresentation presentation = presentationResolver.apply(algorithm);
            if (presentation == null) presentation = new AlgorithmPresentation(algorithm);

            ItemStack item = ItemStack.builder(presentation.icon())
                    .customName(presentation.getCustomName())
                    .lore(presentation.getComponents())
                    .set(ALGO_ID_TAG, algorithm)
                    .build();

            inventory.setItemStack(slots.get(i), item);
            i++;
        }
        return inventory;
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

    private static List<Integer> centeredSlots(int rows, int count) {
        var slots = new ArrayList<Integer>(count);
        int remaining = count;
        for (int row = 0; row < rows && remaining > 0; row++) {
            int inRow = Math.min(9, remaining);
            int leftPad = (9 - inRow) / 2;
            int rowStart = row * 9;
            for (int col = 0; col < inRow; col++) {
                slots.add(rowStart + leftPad + col);
            }
            remaining -= inRow;
        }
        return slots;
    }

    @Override
    public void applyRunningLayout(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setItemStack(0, ItemStack.builder(Material.ENDER_PEARL)
                .customName(Component.text("Randomize"))
                .set(ALGO_INTERACTION_TAG, InteractionType.RANDOMIZE).build());
        inv.setItemStack(1, ItemStack.builder(Material.GREEN_DYE)
                .customName(Component.text("Start"))
                .set(ALGO_INTERACTION_TAG, InteractionType.START).build());
        inv.setItemStack(2, ItemStack.builder(Material.RED_DYE)
                .customName(Component.text("Stop"))
                .set(ALGO_INTERACTION_TAG, InteractionType.STOP).build());
        inv.setItemStack(3, ItemStack.builder(Material.ARROW)
                .customName(Component.text("Step Forward"))
                .set(ALGO_INTERACTION_TAG, InteractionType.FORWARD).build());
        inv.setItemStack(4, ItemStack.builder(Material.SPECTRAL_ARROW)
                .customName(Component.text("Step Back"))
                .set(ALGO_INTERACTION_TAG, InteractionType.BACKWARD).build());
        inv.setItemStack(5, ItemStack.builder(Material.CLOCK)
                .customName(Component.text("Change Speed"))
                .set(ALGO_INTERACTION_TAG, InteractionType.SET_SPEED).build());

        inv.setItemStack(8, ItemStack.builder(Material.BARRIER)
                .customName(Component.text("Clear Algorithm"))
                .set(ALGO_INTERACTION_TAG, InteractionType.CLEAR).build());
    }

    @Override
    public void applyDefaultLayout(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setItemStack(0, ItemStack.builder(Material.NETHER_STAR)
                .customName(Component.text("Algorithm Selector", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(
                        Component.text("Right-click to open the", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.text("algorithm selection menu", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                ).set(ALGO_SELECTOR_TAG, true)
                .build()
        );

        inv.setItemStack(8, ItemStack.builder(Material.COMPASS)
                .customName(Component.text("Return to Hub", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to return to the hub", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .set(ALGO_INTERACTION_TAG, InteractionType.SPAWN)
                .build()
        );
    }
}