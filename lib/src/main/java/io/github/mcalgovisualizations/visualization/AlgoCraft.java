package io.github.mcalgovisualizations.visualization;


import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import io.github.mcalgovisualizations.visualization.ui.IAlgorithmUI;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;

import java.util.*;
import java.util.function.Supplier;

import static io.github.mcalgovisualizations.visualization.ui.Tags.*;


public class AlgoCraft {

    private record AlgorithmEntry(IPlayerSort algorithm, SortingCollection<?> collection, ILayout layout) {
    }

    private IAlgorithmUI ui = new AlgorithmUI();

    private final InstanceContainer instanceContainer;

    private final Map<UUID, VisualizationController> playerSteppers = new HashMap<>();

    private final Map<String, AlgorithmEntry> algorithms = new HashMap<>();


    public AlgoCraft(InstanceContainer instanceContainer) {
        this.instanceContainer = instanceContainer;
    }

    public void addListeners(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            VisualizationController vis = getVisualization(player);
            ItemStack itemStack = event.getItemStack();
            event.setCancelled(true); // Prevent teleportation

            printAll();
            if (itemStack.hasTag(ALGO_SELECTOR_TAG)) {
                selectAlgorithm(player);
                return;
            }

            if (itemStack.hasTag(ALGO_INTERACTION_TAG)) {
                switch (itemStack.getTag(ALGO_INTERACTION_TAG)) {
                    case RANDOMIZE -> vis.randomize();
                    case START -> vis.start();
                    case STOP -> vis.stop();
                    case RESUME -> vis.resume();
                    case FORWARD -> vis.step();
                    case BACKWARD -> vis.back();
                    case CLEAR -> {
                        ui.applyDefaultLayout(player);
                        removeVisualization(player);
                    }
                }
            }
        });
    }

    public <T extends Comparable<T>> void registerAlgorithm(
            String id,
            Supplier<? extends IPlayerSort> ctor,
            List<Data<T>> lst,
            ILayout layout
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ctor, "ctor");

        algorithms.put(id, new AlgorithmEntry(ctor.get(), new SortingCollection<>(lst), layout));

    }

    public void selectAlgorithm(Player player) {
        var inventory = ui.openSelector(algorithms.keySet());
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer() != player) return;
            if (event.getInventory() != inventory) return;

            event.setCancelled(true); // Prevent taking items

            ItemStack clickedItem = event.getClickedItem();
            if (clickedItem.isAir()) return;

            // Find which algorithm was clicked
            for (String algorithm : algorithms.keySet()) {
                var algo_id = clickedItem.getTag(ALGO_ID_TAG);
                if (algo_id == null) continue;
                if (algo_id.equals(algorithm)) {
                    assignVisualization(player, algo_id, instanceContainer, algorithms.get(algorithm).collection, algorithms.get(algorithm).algorithm, algorithms.get(algorithm).layout);
                    ui.applyRunningLayout(player);
                    player.closeInventory();
                    return;
                }
            }
        });
        player.openInventory(inventory);
    }

    public void setSelectorUI(IAlgorithmUI ui) {
        this.ui = ui;
    }

    private void assignVisualization(
            Player player,
            String type, // Todo - fix this so that it's not a string and either determined by the lib or the user.
            InstanceContainer instance,
            SortingCollection<?> collection,
            IPlayerSort playerAlgorithm,
            ILayout layout
    )  {
        removeVisualization(player);

        final var origin = new Pos(0, -60, 0);

        final var renderer = new Renderer(instance, origin, layout);
        final var controller = new VisualizationController(playerAlgorithm, renderer, collection);
        controller.setAudience(player);

        controller.startVisualization();

        playerSteppers.put(player.getUuid(), controller);
    }

    private void removeVisualization(Player player) {
        VisualizationController vis = playerSteppers.remove(player.getUuid());
        if (vis != null) {
            vis.cleanup();
        }
    }

    public VisualizationController getVisualization(Player player) {
        return playerSteppers.get(player.getUuid());
    }

    public void printAll() {
        System.out.println("playerSteppers: " + playerSteppers);
        System.out.println("algorithms: " + algorithms);
    }
}