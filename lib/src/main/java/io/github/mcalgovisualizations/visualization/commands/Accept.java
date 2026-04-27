package io.github.mcalgovisualizations.visualization.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class Accept extends Command {

    public Accept(AlgoCraft algoCraft) {
        super("accept");

        var targetArg = ArgumentType.Entity("target").onlyPlayers(true);

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var invites = algoCraft.getPendingInvites(player);

            if (invites.isEmpty()) {
                player.sendMessage(Component.text("No pending invites.", NamedTextColor.RED));
                return;
            }

            player.sendMessage(Component.text("Your pending invites:", NamedTextColor.GREEN));

            for (var invite : invites) {
                var inviter = MinecraftServer.getConnectionManager()
                        .getOnlinePlayerByUuid(invite.inviter());

                var inviterName = inviter != null
                        ? inviter.getUsername()
                        : invite.inviter().toString();

                player.sendMessage(Component.text("- " + inviterName, NamedTextColor.YELLOW));
            }

            player.sendMessage(Component.text("Use /accept <player> to join.", NamedTextColor.GRAY));
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var targets = context.get(targetArg).find(sender);

            if (targets.isEmpty()) {
                player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return;
            }

            var inviter = targets.getFirst();

            if (!(inviter instanceof Player inviterPlayer)) {
                player.sendMessage(Component.text("Target must be a player.", NamedTextColor.RED));
                return;
            }

            boolean accepted = algoCraft.acceptInvite(player, inviterPlayer);

            if (accepted) {
                player.sendMessage(Component.text(
                        "Accepted invite from " + inviterPlayer.getUsername() + ".",
                        NamedTextColor.GREEN
                ));
            }
        }, targetArg);
    }
}