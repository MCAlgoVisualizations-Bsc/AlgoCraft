package io.github.mcalgovisualizations.commands;

import io.github.mcalgovisualizations.visualization.AlgoCraft;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class testCommand extends Command {
    public testCommand(AlgoCraft a) {
        super("test", "t");

        addSyntax((sender, _) -> {
            if (sender instanceof Player)
                a.printAll();
        });
    }
}
