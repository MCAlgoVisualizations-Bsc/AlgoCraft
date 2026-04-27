package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.kyori.adventure.text.Component;
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
                    var message = Component.text(" - " + pendingInvite.inviter().toString() + ", expires in " + System.currentTimeMillis() + pendingInvite.expiresAt());

                    player.sendMessage(message);
                });

            }


        });
    }
}
