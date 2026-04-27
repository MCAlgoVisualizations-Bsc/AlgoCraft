package io.github.mcalgovisualizations.visualization.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.visualization.instance.AlgorithmInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.time.Duration;

public class Accept extends Command {

    public Accept(AlgoCraft algoCraft) {
        super("accept");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var invite = algoCraft.getPendingInvites().remove(player.getUuid());

            if (invite == null) {
                player.sendMessage("No pending invites.");
                return;
            }

            if (System.currentTimeMillis() > invite.expiresAt) {
                player.sendMessage("Invite expired.");
                return;
            }

            var instance = invite.instance;

            // join new instance
            instance.addPlayer(player);

            // IMPORTANT: delay teleport to avoid chunk issue
            MinecraftServer.getSchedulerManager()
                    .buildTask(() -> player.teleport(AlgorithmInstance.INSTANCE_ORIGIN))
                    .delay(Duration.ofMillis(100))
                    .schedule();

            player.sendMessage("Joined instance.");
        });
    }
}
