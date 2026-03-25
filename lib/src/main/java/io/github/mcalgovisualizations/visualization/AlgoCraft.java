package io.github.mcalgovisualizations.visualization;


import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.IAlgorithmUI;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.mcalgovisualizations.visualization.ui.Tags.*;


public class AlgoCraft {
    public record AlgorithmPlacement(@NotNull Pos renderOrigin, @NotNull Pos teleportPoint) { }

    private record AlgorithmEntry(IPlayerSort algorithm, SortingCollection<?> collection, ILayout layout, AlgorithmPlacement placement) {
        @Override
        public SortingCollection<?> collection() {
            return this.collection.copy();
        }
    }

    private IAlgorithmUI ui = new AlgorithmUI();

    private final InstanceContainer instanceContainer;

    private final Map<UUID, VisualizationController> playerSteppers = new HashMap<>();

    private final Map<String, AlgorithmEntry> algorithms = new HashMap<>();

    private final Map<String, AlgorithmPresentation> algorithmPresentations = new HashMap<>();

    private Consumer<Player> spawnAction = player -> {};


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
                    case RANDOMIZE -> {
                        if (vis != null) vis.randomize();
                    }
                    case START -> {
                        if (vis != null) vis.start();
                    }
                    case STOP -> {
                        if (vis != null) vis.stop();
                    }
                    case RESUME -> {
                        if (vis != null) vis.resume();
                    }
                    case FORWARD -> {
                        if (vis != null) vis.step();
                    }
                    case BACKWARD -> {
                        if (vis != null) vis.back();
                    }
                    case CLEAR -> {
                        ui.applyDefaultLayout(player);
                        removeVisualization(player);
                    }
                    case SPAWN -> spawnAction.accept(player);
                }
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> removeVisualization(playerDisconnectEvent.getPlayer()));
    }

    public <T extends Comparable<T>> void registerAlgorithm(
            @NotNull String id,
            @NotNull Supplier<? extends IPlayerSort> ctor,
            @NotNull List<Data<T>> lst,
            @NotNull ILayout layout,
            @NotNull AlgorithmPlacement placement
    ) {
        algorithms.put(id, new AlgorithmEntry(ctor.get(), new SortingCollection<>(lst), layout, placement));
    }

    public void selectAlgorithm(Player player) {
        var inventory = ui.openSelector(algorithms.keySet(), this::resolvePresentation);
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer() != player) return;
            if (event.getInventory() != inventory) return;

            event.setCancelled(true); // Prevent taking items

            ItemStack clickedItem = event.getClickedItem();
            if (clickedItem.isAir()) return;

            var algorithmId = clickedItem.getTag(ALGO_ID_TAG);
            if (algorithmId == null) return;

            var entry = algorithms.get(algorithmId);
            if (entry == null) return;

            assignVisualization(player, instanceContainer, entry.collection(), entry.algorithm(), entry.layout(), entry.placement.renderOrigin());
            player.teleport(entry.placement.teleportPoint());

            ui.applyRunningLayout(player);
            player.closeInventory();
        });
        player.openInventory(inventory);
    }

    public void setSelectorUI(IAlgorithmUI ui) {
        this.ui = ui;
    }

    public void registerAlgorithmPresentation(String algorithmId, AlgorithmPresentation presentation) {
        algorithmPresentations.put(algorithmId, presentation);
    }

    public void setSpawnAction(Consumer<Player> spawnAction) {
        this.spawnAction = spawnAction == null ? player -> {} : spawnAction;
    }

    public void applyDefaultLayout(Player player) {
        ui.applyDefaultLayout(player);
    }

    private AlgorithmPresentation resolvePresentation(String algorithmId) {
        AlgorithmPresentation presentation = algorithmPresentations.get(algorithmId);
        if (presentation != null) return presentation;

        return AlgorithmPresentation.fallback(algorithmId);
    }

    private void assignVisualization(
            Player player,
            InstanceContainer instance,
            SortingCollection<?> collection,
            IPlayerSort playerAlgorithm,
            ILayout layout,
            Pos renderOrigin
    )  {
        removeVisualization(player);

        final var renderer = new Renderer(instance, renderOrigin, layout);
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