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

import java.util.Set;
import java.util.function.Function;

import static io.github.mcalgovisualizations.visualization.ui.Tags.*;

public class AlgorithmUI implements IAlgorithmUI {
    private static final int[] CENTERED_SLOTS = {10, 12, 14, 16, 18, 20, 22, 24, 26};

    @Override
    public Inventory openSelector(Set<String> algorithms, Function<String, AlgorithmPresentation> presentationResolver) {
        if (algorithms.size() > CENTERED_SLOTS.length) {
            throw new RuntimeException("Too many algorithms for centered selector, max is " + CENTERED_SLOTS.length);
        }

        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Select Algorithm", NamedTextColor.DARK_PURPLE));

        int i = 0;
        for (String algorithm : algorithms) {
            AlgorithmPresentation presentation = presentationResolver.apply(algorithm);
            if (presentation == null) {
                presentation = AlgorithmPresentation.fallback(algorithm);
            }

            ItemStack item = ItemStack.builder(presentation.icon())
                    .customName(Component.text(presentation.displayName(), NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false))
                    .lore(
                            Component.text(presentation.description1(), NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false),
                            Component.text(presentation.description2(), NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false),
                            Component.empty(),
                            Component.text(presentation.complexity(), NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, false),
                            Component.empty(),
                            Component.text("Click to select!", NamedTextColor.GREEN)
                                    .decoration(TextDecoration.ITALIC, false)
                    )
                    .set(ALGO_ID_TAG, algorithm)
                    .build();
            inventory.setItemStack(CENTERED_SLOTS[i], item);
            i++;
        }

        return inventory;
    }

    @Override
    public void applyRunningLayout(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setItemStack(0,
                ItemStack.builder(Material.ENDER_PEARL)
                        .customName(Component.text("Randomize"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.RANDOMIZE)
                        .build()
        );
        inv.setItemStack(1,
                ItemStack.builder(Material.GREEN_DYE)
                .customName(Component.text("Start"))
                .set(ALGO_INTERACTION_TAG, InteractionType.START)
                .build()
        );
        inv.setItemStack(2,
                ItemStack.builder(Material.RED_DYE)
                        .customName(Component.text("Stop"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.STOP)
                        .build()
        );
        inv.setItemStack(3,
                ItemStack.builder(Material.ARROW)
                        .customName(Component.text("Step Forward"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.FORWARD)
                        .build()
        );
        inv.setItemStack(4,
                ItemStack.builder(Material.SPECTRAL_ARROW)
                        .customName(Component.text("Step Back"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.BACKWARD)
                        .build()
        );
        inv.setItemStack(5,
                ItemStack.builder(Material.CLOCK)
                        .customName(Component.text("Change Speed"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.SET_SPEED)
                        .build()

        );
        inv.setItemStack(8,
                ItemStack.builder(Material.BARRIER)
                        .customName(Component.text("Clear Algorithm"))
                        .set(ALGO_INTERACTION_TAG, InteractionType.CLEAR)
                        .build()
        );
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
                        Component.text("Right-click to open the", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("algorithm selection menu", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ).set(ALGO_SELECTOR_TAG, true)
                .build()
        );

        inv.setItemStack(8, ItemStack.builder(Material.COMPASS)
                .customName(Component.text("Return to Hub", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to return to the hub", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .set(ALGO_INTERACTION_TAG, InteractionType.SPAWN)
                .build()
        );
    }
}
