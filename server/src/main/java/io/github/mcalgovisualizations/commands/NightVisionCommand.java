package io.github.mcalgovisualizations.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NightVisionCommand extends Command {

    public NightVisionCommand() {
        super("nv");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command is for players only!");
                return;
            }

            boolean hasNV = player.getActiveEffects().stream()
                    .anyMatch(potion -> potion.potion().effect() == PotionEffect.NIGHT_VISION);

            if (hasNV) {
                player.removeEffect(PotionEffect.NIGHT_VISION);
                player.sendMessage(Component.text("Night vision disabled.", NamedTextColor.RED));
            } else {
                Potion nvPotion = new Potion(PotionEffect.NIGHT_VISION, (byte) 0, Potion.INFINITE_DURATION);
                player.addEffect(nvPotion);
                player.sendMessage(Component.text("Night vision enabled.", NamedTextColor.GREEN));
            }
        });
    }
}