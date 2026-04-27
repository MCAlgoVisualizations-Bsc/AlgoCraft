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
            if (targets.isEmpty()) {
                player.sendMessage("No player found with username: " + context.getInput());
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
