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
            p.sendMessage("Teleporting to spawn...");
            p.setInstance(algo.getDefaultInstance())
                    .thenRun(() -> p.sendMessage("Teleported to spawn!"))
                    .thenRun(() -> p.teleport(new Pos(194, 137, -38)));
        });
    }
}