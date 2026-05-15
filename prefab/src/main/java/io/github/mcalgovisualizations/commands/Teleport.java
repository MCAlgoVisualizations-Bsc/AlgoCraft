package io.github.mcalgovisualizations.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

public class Teleport extends Command {

    public Teleport() {
        super("tp");

        setDefaultExecutor((sender, _) -> {
            sender.sendMessage("Usage: /tp <target> or <x> <y> <z> or <target> <target>");
        });

        registerTpToPlayer();
        registerTpPlayerToPlayer();
        registerTpToCoordinates();
    }

    /**
     * Command to teleport from the current player to another player.
     */
    private void registerTpToPlayer() {
        var targetArg = new ArgumentEntity("target").onlyPlayers(true);

        setDefaultExecutor((sender, _) -> {
            sender.sendMessage("Usage: /tp <target>");
        });

        targetArg.setCallback((sender, exception) -> {
            final String input = exception.getInput();
            sender.sendMessage("The target " + input + " is invalid!");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            EntityFinder finder = context.get(targetArg);

            var entities = finder.find(sender);
            var it = entities.iterator();
            if (!it.hasNext()) return;

            Player target = (Player) it.next();
            target.teleport(player.getPosition());
        }, targetArg);
    }

    /**
     * Command to teleport one player to another player
     */
    private void registerTpPlayerToPlayer() {
        var fromPlayer =  new ArgumentEntity("fromPlayer").onlyPlayers(true);
        var targetPlayer = new ArgumentEntity("targetPlayer").onlyPlayers(true);

        fromPlayer.setCallback((sender, exception) -> {
            final String input = exception.getInput();
            sender.sendMessage("The target " + input + " is invalid!");
        });

        targetPlayer.setCallback((sender, exception) -> {
            final String input = exception.getInput();
            sender.sendMessage("The target " + input + " is invalid!");
        });

        addSyntax((sender, context) -> {
            EntityFinder fromFinder = context.get(fromPlayer);
            EntityFinder targetFinder = context.get(targetPlayer);

            var fromEntities = fromFinder.find(sender);
            var targetEntities = targetFinder.find(sender);

            var fromIt = fromEntities.iterator();
            if (!fromIt.hasNext()) return;

            var targetIt = targetEntities.iterator();
            if (!targetIt.hasNext()) return;

            Player fromTarget = (Player) fromIt.next();
            Player targetTarget = (Player) targetIt.next();

            fromTarget.teleport(targetTarget.getPosition());
        }, fromPlayer, targetPlayer);
    }

    /**
     * Command to teleport from the current player to a specified coordinate
     */
    private void registerTpToCoordinates() {
        var x = new ArgumentDouble("x");
        var y = new ArgumentDouble("y");
        var z = new ArgumentDouble("z");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            player.teleport(
                    new Pos(
                            context.get(x),
                            context.get(y),
                            context.get(z)
                    )
            );
        });
    }
}