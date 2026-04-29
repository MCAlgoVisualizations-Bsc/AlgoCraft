package io.github.mcalgovisualizations.ui;

import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public class VillagerPovAlgorithmUI extends AlgorithmUI {
    public static final Tag<Byte> POV_TOGGLE_TAG = Tag.Byte("pov_toggle");

    @Override
    public void applyRunningLayout(Player player) {
        super.applyRunningLayout(player);
        player.getInventory().setItemStack(6,
                ItemStack.builder(Material.SPYGLASS)
                        .customName(Component.text("Villager POV"))
                        .set(POV_TOGGLE_TAG, (byte) 1)
                        .build()
        );
    }
}

