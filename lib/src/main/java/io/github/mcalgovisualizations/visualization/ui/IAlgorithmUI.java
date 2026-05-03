package io.github.mcalgovisualizations.visualization.ui;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;

import java.util.Set;
import java.util.function.Function;

public interface IAlgorithmUI {
    Inventory openSelector(Set<String> algorithms, Function<String, AlgorithmPresentation> presentationResolver);
    void applyRunningLayout(Player player);
    void applyDefaultLayout(Player player, Instance spawnInstance);
}
