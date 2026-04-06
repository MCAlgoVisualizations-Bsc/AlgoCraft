package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.*;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.mcalgovisualizations.visualization.ui.InteractionType.SPAWN;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_ID_TAG;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_INTERACTION_TAG;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_SELECTOR_TAG;

public final class AlgoCraft {

    private record AlgorithmEntry<T extends Comparable<T>, O extends ISceneOps>(
            IPlayerSort algorithm,
            ISort<T> collection,
            ILayout layout,
            AlgorithmPlacement placement,
            Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> eventHandlers,
            Function<? super ISort<T>, ? extends AnimationPlan<O>> completeHandler,
            Function<SceneContext, O> scene
    ) {
        @Override
        public ISort<T> collection() {
            return this.collection.copy();
        }
    }

    private IAlgorithmUI ui = new AlgorithmUI();
    private final InstanceContainer instanceContainer;
    private final Map<UUID, PlayerControls> playerSteppers = new HashMap<>();
    private final Map<String, AlgorithmEntry<?, ?>> algorithms = new HashMap<>();
    private final Map<String, AlgorithmPresentation> algorithmPresentations = new HashMap<>();
    private Consumer<Player> spawnAction = player -> {};

    public AlgoCraft(InstanceContainer instanceContainer) {
        this.instanceContainer = instanceContainer;
    }

    public void addListeners(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> {
            final var player = event.getPlayer();
            final var controls = getVisualization(player);
            final ItemStack itemStack = event.getItemStack();
            event.setCancelled(true); // Prevent teleportation

            if (itemStack.hasTag(ALGO_SELECTOR_TAG)) {
                selectAlgorithm(player);
                return;
            }

            if(itemStack.hasTag(ALGO_INTERACTION_TAG) && itemStack.getTag(ALGO_INTERACTION_TAG).equals(SPAWN)) {
                spawnAction.accept(player);
                return;
            }

            if(controls == null) {
                System.err.println("No controls for player " + player.getUsername());
                return;
            }

            if (itemStack.hasTag(ALGO_INTERACTION_TAG)) {
                switch (itemStack.getTag(ALGO_INTERACTION_TAG)) {
                    case RANDOMIZE -> controls.randomize();
                    case START -> controls.start();
                    case STOP -> controls.pause();
                    case FORWARD -> controls.step();
                    case BACKWARD -> controls.back();
                    case SET_SPEED -> controls.changeSpeed();
                    default -> {
                        ui.applyDefaultLayout(player);
                        removeVisualization(player);
                    }
                }
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> removeVisualization(playerDisconnectEvent.getPlayer()));
    }

    public <T extends Comparable<T>, O extends ISceneOps> void registerAlgorithm(Algorithm<T, O> algo) {
        algorithms.put(
                algo.id(),
                new AlgorithmEntry<>(
                        algo.ctor().get(),
                        new SortingCollection<>(algo.model()),
                        algo.layout(),
                        algo.placement(),
                        algo.handlerRegistry(),
                        algo.onComplete(),
                        algo.scene()
                )
        );
        if (algo.presentation() != null)
            algorithmPresentations.put(algo.id(), algo.presentation());
    }

    public void selectAlgorithm(Player player) {
        final var inventory = ui.openSelector(algorithms.keySet(), this::resolvePresentation);

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer() != player) return;
            if (event.getInventory() != inventory) return;

            event.setCancelled(true); // Prevent taking items

            ItemStack clickedItem = event.getClickedItem();
            if (clickedItem.isAir()) return;

            final var algorithmId = clickedItem.getTag(ALGO_ID_TAG);
            if (algorithmId == null) return;

            final var entry = algorithms.get(algorithmId);
            if (entry == null) return;

            assignVisualization(player, instanceContainer, entry);
            player.teleport(entry.placement().teleportPoint());

            ui.applyRunningLayout(player);
            player.closeInventory();
        });

        player.openInventory(inventory);
    }

    public void setSelectorUI(IAlgorithmUI ui) {
        this.ui = ui;
    }

    public void setSpawnAction(Consumer<Player> spawnAction) {
        this.spawnAction = spawnAction == null ? player -> {} : spawnAction;
    }

    public void applyDefaultLayout(Player player) {
        ui.applyDefaultLayout(player);
    }

    public PlayerControls getVisualization(Player player) {
        return playerSteppers.get(player.getUuid());
    }

    private AlgorithmPresentation resolvePresentation(String algorithmId) {
        AlgorithmPresentation presentation = algorithmPresentations.get(algorithmId);
        return presentation != null
                ? presentation
                : AlgorithmPresentation.fallback(algorithmId);
    }

    private <T extends Comparable<T>, O extends ISceneOps> void assignVisualization(
            Player player,
            InstanceContainer instance,
            AlgorithmEntry<T, O> entry
    ) {
        assignVisualizationCaptured(player, instance, entry);
    }

    private <T extends Comparable<T>, O extends ISceneOps> void assignVisualizationCaptured(
            Player player,
            InstanceContainer instance,
            AlgorithmEntry<T, O> algo
    ) {
        removeVisualization(player);
        // implementation for sounds and messages directly to the player
        final var audience = new PlayerFeedback(player);

        // should probably not be here, but it works for now
        // sorts the collection and returns the complete plan,
        // so users can make onCompletion with the sorted collection
        final var collection = algo.collection().copy();
        final var algorithm = algo.algorithm();
        algorithm.sort(collection);
        final var onCompletePlan = algo.completeHandler().apply(collection);
        final var scene = algo.scene().apply(new SceneContext(instance, audience, algo.placement().renderOrigin()));


        final var renderer = new Renderer<O>(
                instance,
                algo.placement().renderOrigin(),
                algo.layout(),
                audience,
                algo.eventHandlers(),
                onCompletePlan,
                scene
        );

        final var controller = new VisualizationController<T>(
                algo.algorithm(),
                renderer,
                algo.collection(),
                audience
        );

        controller.startVisualization();
        playerSteppers.put(player.getUuid(), controller);
    }

    private void removeVisualization(Player player) {
        final var controls = playerSteppers.remove(player.getUuid());
        if (controls != null) {
            controls.clear();
        }
    }
}