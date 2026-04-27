package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class PendingInvites extends Command {
    public PendingInvites(AlgoCraft algo) {
        super("pendingInvites");

        setDefaultExecutor((sender, context) -> {
            if(sender instanceof Player player) {
                var pending = algo.getPendingInvites(player);
                if (pending.isEmpty()) {
                    sender.sendMessage("You have no pending invites.");
                    return;
                }


                sender.sendMessage("You have " + pending.size() + " pending invites:");
                pending.forEach( pendingInvite -> {
                    final var inviter = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(pendingInvite.inviter());
                    if(inviter != null) {
                        var message = Component.text(" - " + inviter.getUsername() + ", expires in: " + pendingInvite);
                        player.sendMessage(message);
                    }
                });

            }


        });
    }
}
