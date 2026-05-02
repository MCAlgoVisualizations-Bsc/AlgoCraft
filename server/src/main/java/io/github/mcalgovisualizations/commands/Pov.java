package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.Villager.VillagerPovManager;
import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.visualization.instance.PlayerFeedback;
import io.github.mcalgovisualizations.visualization.ui.InteractionType;
import io.github.mcalgovisualizations.visualization.ui.Tags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class Pov extends Command {
    private final AlgoCraft algo;
    private final VillagerPovManager povManager;

    public Pov(AlgoCraft algo, VillagerPovManager povManager) {
        super("pov");
        this.algo = algo;
        this.povManager = povManager;

        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof Player player)) return;

            povManager.resolveScene(player).ifPresentOrElse(scene -> {
                        if (povManager.toggle(player, scene, new PlayerFeedback(() -> player))) {
                            applyRunningHotbar(player);
                        }
                    },
                    () -> player.sendMessage(Component.text(
                            "Villager POV is only available when your current algorithm has a villager/searcher.",
                            NamedTextColor.RED
                    ))
            );
        });
    }

    private static void applyRunningHotbar(Player player) {
        PlayerInventory inv = player.getInventory();

        inv.setItemStack(0, ItemStack.builder(Material.ENDER_PEARL)
                .customName(Component.text("Randomize"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.RANDOMIZE)
                .build());
        inv.setItemStack(1, ItemStack.builder(Material.GREEN_DYE)
                .customName(Component.text("Start"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.START)
                .build());
        inv.setItemStack(2, ItemStack.builder(Material.RED_DYE)
                .customName(Component.text("Stop"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.STOP)
                .build());
        inv.setItemStack(3, ItemStack.builder(Material.ARROW)
                .customName(Component.text("Step Forward"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.FORWARD)
                .build());
        inv.setItemStack(4, ItemStack.builder(Material.SPECTRAL_ARROW)
                .customName(Component.text("Step Back"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.BACKWARD)
                .build());
        inv.setItemStack(5, ItemStack.builder(Material.CLOCK)
                .customName(Component.text("Change Speed"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.SET_SPEED)
                .build());
        inv.setItemStack(8, ItemStack.builder(Material.BARRIER)
                .customName(Component.text("Clear Algorithm"))
                .set(Tags.ALGO_INTERACTION_TAG, InteractionType.CLEAR)
                .build());
    }
}