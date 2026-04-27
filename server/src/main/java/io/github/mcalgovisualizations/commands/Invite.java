package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class Invite extends Command {

    public Invite(AlgoCraft algoCraft) {
        super("invite");

        var targetArg = ArgumentType.Entity("target").onlyPlayers(true);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var targets = context.get(targetArg).find(sender);
            if (targets.isEmpty()) {
                player.sendMessage("No player found with username: " + context.getInput());
                return;
            }

            if(player == targets.getFirst()) {
                player.sendMessage(Component.text("You cannot invite yourself", NamedTextColor.RED));
                return;
            }

            Player target = (Player) targets.getFirst();
            long ttl = System.currentTimeMillis() + 1000 * 60 * 60;
            algoCraft.invitePlayer(player, target, ttl);
        }, targetArg);

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("Usage: /invite <player>");
            }
        });
    }
}
