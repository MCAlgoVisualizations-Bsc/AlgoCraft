package io.github.mcalgovisualizations.visualization.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.visualization.instance.AlgorithmInstance;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;

import java.time.Duration;

public class Invite extends Command {

    public Invite(AlgoCraft algoCraft) {
        super("invite");

        var targetArg = ArgumentType.Entity("target").onlyPlayers(true);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            var targets = context.get(targetArg).find(sender);
            if (targets.isEmpty()) return;

            Player target = (Player) targets.getFirst();

            // cannot invite yourself
            if (target == player) {
                player.sendMessage("You cannot invite yourself.");
                return;
            }

            // must be in an instance
            AlgorithmInstance<?, ?, ?> instance;
            try {
                instance = algoCraft.requireInstance(player);
            } catch (Exception e) {
                player.sendMessage("You are not in an instance.");
                return;
            }

            // create invite (30s expiry)
            algoCraft.getPendingInvites().put(
                    target.getUuid(),
                    new AlgoCraft.PendingInvite(
                            player.getUuid(),
                            instance,
                            System.currentTimeMillis() + Duration.ofSeconds(30).toMillis()
                    )
            );

            player.sendMessage("Invited " + target.getUsername());
            target.sendMessage(player.getUsername() + " invited you. Type /accept to join.");

        }, targetArg);

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("Usage: /invite <player>");
            }
        });
    }
}
