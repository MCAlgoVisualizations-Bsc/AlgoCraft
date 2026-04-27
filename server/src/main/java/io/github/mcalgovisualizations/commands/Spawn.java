package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public class Spawn extends Command {
    public Spawn(AlgoCraft algo) {
        super("spawn");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player p)) return;
            algo.removePlayerFromInstance(p);
        });
    }
}