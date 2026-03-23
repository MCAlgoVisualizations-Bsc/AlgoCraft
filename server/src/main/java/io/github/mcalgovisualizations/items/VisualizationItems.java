package io.github.mcalgovisualizations.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_SELECTOR_TAG;

// could this be serialized?
/**
 * Factory class for creating control items used to interact with visualizations.
 * Players use these items in their hotbar to control the algorithm playback.
 */
public final class VisualizationItems {

    private VisualizationItems() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates the randomize item (Ender Pearl).
     * When used, randomizes the values in the current visualization.
     */
    public static ItemStack randomizeItem() {
        return ItemStack.builder(Material.ENDER_PEARL)
                .customName(Component.text("Randomize", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to randomize values", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the start item (Lime Dye).
     * When used, starts the automatic visualization playback.
     */
    public static ItemStack startItem() {
        return ItemStack.builder(Material.LIME_DYE)
                .customName(Component.text("Start", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to start visualization", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the stop item (Red Dye).
     * When used, stops the visualization playback.
     */
    public static ItemStack stopItem() {
        return ItemStack.builder(Material.RED_DYE)
                .customName(Component.text("Stop", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to stop visualization", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the step forward item (Arrow).
     * When used, advances the visualization by one step.
     */
    public static ItemStack stepForwardItem() {
        return ItemStack.builder(Material.ARROW)
                .customName(Component.text("Step Forward", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to step forward one step", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the step back item (Spectral Arrow).
     * When used, goes back one step in the visualization history.
     */
    public static ItemStack stepBackItem() {
        return ItemStack.builder(Material.SPECTRAL_ARROW)
                .customName(Component.text("Step Back", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to step back one step", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the spawn/hub return item (Compass).
     * When used, teleports the player back to the central hub.
     */
    public static ItemStack spawnItem() {
        return ItemStack.builder(Material.COMPASS)
                .customName(Component.text("Return to Hub", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Right-click to return to the hub", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .build();
    }

    /**
     * Creates the algorithm selector item (Nether Star).
     * When used, opens a GUI to select which algorithm to visualize.
     */
    public static ItemStack algorithmSelectorItem() {
        return ItemStack.builder(Material.NETHER_STAR)
                .customName(Component.text("Algorithm Selector", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(
                        Component.text("Right-click to open the", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("algorithm selection menu", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ).set(ALGO_SELECTOR_TAG, true)
                .build();
    }
}