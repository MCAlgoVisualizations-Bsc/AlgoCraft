package io.github.mcalgovisualizations.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public class Spawn extends Command {
    public Spawn() {
        super("spawn");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player)) return;
            sender.sendMessage("Returning to spawn!");
            ((Player) sender).teleport(new Pos(194, 137, -38));
        });
    }
}