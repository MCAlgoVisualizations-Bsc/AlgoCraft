package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithm.AlgorithmTraceBuilder;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.*;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static io.github.mcalgovisualizations.visualization.ui.InteractionType.SPAWN;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_ID_TAG;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_INTERACTION_TAG;
import static io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_SELECTOR_TAG;

public final class AlgoCraft {

    private static final class VisualizationSession {
        private final VisualizationController controls;
        private final PlayerFeedback audience;

        VisualizationSession(VisualizationController controls, Audience... audience) {
            this.controls = controls;
            this.audience = new PlayerFeedback(audience);
        }

        PlayerControls controls() {
            return controls;
        }

        void start() {
            controls.startVisualization();
        }

        void clear() {
            controls.clear();
        }

        PlayerFeedback audience() {
            return audience;
        }
    }

    private IAlgorithmUI ui = new AlgorithmUI();
    private final InstanceContainer instanceContainer;
    private final Map<UUID, VisualizationSession> sessions = new HashMap<>();
    private final Map<String, Algorithm<?, ?, ?>> algorithms = new HashMap<>();
    private final Map<String, AlgorithmPresentation> algorithmPresentations = new HashMap<>();
    private Consumer<Player> spawnAction = player -> {
    };

    public AlgoCraft(InstanceContainer instanceContainer) {
        this.instanceContainer = instanceContainer;
    }

    public void addListeners(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> {
            final var player = event.getPlayer();
            final var session = getSession(player.getUuid());
            final ItemStack itemStack = event.getItemStack();
            event.setCancelled(true); // Prevent teleportation

            if (itemStack.hasTag(ALGO_SELECTOR_TAG)) {
                selectAlgorithm(player);
                return;
            }

            if (itemStack.hasTag(ALGO_INTERACTION_TAG) && itemStack.getTag(ALGO_INTERACTION_TAG).equals(SPAWN)) {
                spawnAction.accept(player);
                return;
            }

            if (session == null) {
                System.err.println("No controls for player " + player.getUsername());
                return;
            }


            if (itemStack.hasTag(ALGO_INTERACTION_TAG)) {
                switch (itemStack.getTag(ALGO_INTERACTION_TAG)) {
                    case RANDOMIZE -> session.controls().randomize();
                    case START -> session.controls().start();
                    case STOP -> session.controls().pause();
                    case FORWARD -> session.controls().step();
                    case BACKWARD -> session.controls().back();
                    case SET_SPEED -> session.controls().changeSpeed();
                    default -> {
                        ui.applyDefaultLayout(player);
                        removeVisualization(player);
                    }
                }
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> removeVisualization(playerDisconnectEvent.getPlayer()));
    }


    @SafeVarargs
    public final <T, C extends AlgorithmContext<T>, O extends ISceneOps>
    void registerAlgorithm(Algorithm<T, C, O>... algorithm) {
        for (Algorithm<T, C, O> a : algorithm) {
            algorithms.put(a.id(), a);
        }
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

            var originalpos = player.getPosition();

            player.closeInventory();
            player.teleport(entry.placement().teleportPoint());
            ui.applyRunningLayout(player);

            MinecraftServer.getSchedulerManager()
                    .buildTask(() -> {
                        try {
                            var session = assignVisualization(entry, instanceContainer, player);
                            session.start();
                        } catch (IllegalStateException e) {
                            player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                        } catch (NullPointerException e) {
                            player.sendMessage(Component.text("Failed to assign visualization : ", NamedTextColor.RED));
                            player.teleport(originalpos);
                            ui.applyDefaultLayout(player);
                        }
                    })
                    .delay(Duration.ofMillis(200))
                    .schedule();
        });

        player.openInventory(inventory);
    }

    public void setSelectorUI(IAlgorithmUI ui) {
        this.ui = ui;
    }

    public void setSpawnAction(Consumer<Player> spawnAction) {
        this.spawnAction = spawnAction == null ? _ -> {
        } : spawnAction;
    }

    public void applyDefaultLayout(Player player) {
        ui.applyDefaultLayout(player);
    }

    private VisualizationSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    private AlgorithmPresentation resolvePresentation(String algorithmId) {
        AlgorithmPresentation presentation = algorithmPresentations.get(algorithmId);
        return presentation != null
                ? presentation
                : new AlgorithmPresentation(algorithmId);
    }

    private <T, C extends AlgorithmContext<T>, O extends ISceneOps> VisualizationSession assignVisualization(
            Algorithm<T, C, O> entry, InstanceContainer instance, Player player
    ) {
        try {
            return assignVisualizationCaptured(instance, entry, player);
        } catch (IllegalStateException e) {
            System.err.println("Failed to assign visualization: " + e.getMessage());
            throw e;
        }
    }

    private <T, C extends AlgorithmContext<T>, O extends ISceneOps> VisualizationSession assignVisualizationCaptured(
            InstanceContainer instance,
            Algorithm<T, C, O> algo,
            Player player
    ) {
        removeVisualization(player);

        final var audience = new PlayerFeedback(player);
        final var algoCtx = algo.contextFactory().create(algo.model().getData());


        final var algorithm = algo.ctor().get();
        algorithm.run(algoCtx);
        final AnimationPlan<O> onCompletePlan = algo.onComplete().apply(algoCtx);

        final var sceneCtx = new SceneContext(instance, audience, algo.placement().renderOrigin());
        final var scene = algo.scene().apply(sceneCtx);

        final var renderer = new Renderer<>(
                instance,
                algo.placement().renderOrigin(),
                algo.layout(),
                audience,
                algo.handlerRegistry(),
                onCompletePlan,
                scene
        );

        final var traceBuilder = new AlgorithmTraceBuilder<>(algorithm, algo.model().getData(), algo.contextFactory());

        final var controller = new VisualizationController<>(
                renderer,
                traceBuilder,
                audience
        );

        var session = new VisualizationSession(controller);
        sessions.put(player.getUuid(), session);
        return session;
    }

    private void removeVisualization(Player player) {
        final var session = sessions.remove(player.getUuid());
        if (session != null) {
            session.clear();
        }
    }
}