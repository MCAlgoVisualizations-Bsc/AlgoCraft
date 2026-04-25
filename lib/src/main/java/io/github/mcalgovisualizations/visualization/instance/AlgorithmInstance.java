package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.engine.PlayerControls;
import io.github.mcalgovisualizations.visualization.algorithm.AlgorithmTraceBuilder;
import io.github.mcalgovisualizations.visualization.engine.VisualizationController;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.PlayerFeedback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AlgorithmInstance<T, C extends AlgorithmContext<T>, O extends ISceneOps> {
    private final Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
    private final AlgorithmPresentation presentation;
    private final PlayerFeedback audience;
    private final VisualizationController<T, C> controller;
    public static final Pos origin = new Pos(0, 40, 0);

    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Player... players) {
        this.presentation = algorithm.presentation();
        this.audience = new PlayerFeedback(players);

        instance.setGenerator(unit -> {
            final Point start = unit.absoluteStart();
            final Point size = unit.size();
            for (int x = 0; x < size.blockX(); x++) {
                for (int z = 0; z < size.blockZ(); z++) {
                    for (int y = 0; y < Math.min(40 - start.blockY(), size.blockY()); y++) {
                        unit.modifier().setBlock(start.add(x, y, z), Block.STONE);
                    }
                }
            }
        });

        instance.setChunkSupplier(LightingChunk::new);

        //container.setChunkLoader(new AnvilLoader(worldPath));

        final var algorithmCtx = algorithm.model();
        final var a = algorithm.ctor().get();
        a.run(algorithmCtx);
        final AnimationPlan<O> onCompletePlan = algorithm.onComplete().apply(algorithmCtx);

        final var sceneCtx = new SceneContext(instance, audience, new Pos(0, 40, 0));
        final var scene = algorithm.scene().apply(sceneCtx);

        final var renderer = new Renderer<>(
                instance,
                new Pos(0, 40, 0),
                algorithm.layout(),
                audience,
                algorithm.handlerRegistry(),
                onCompletePlan,
                scene
        );

        final var traceBuilder = new AlgorithmTraceBuilder<>(
                a,
                algorithm.model()
        );

        this.controller = new VisualizationController<>(
                renderer,
                traceBuilder,
                audience
        );

    }

    public Instance getInstance() {
        return this.instance;
    }

    public boolean containsPlayer(Player... players) {
        return this.audience.containsPlayer(players);
    }

    public CompletableFuture<Void> addPlayer(Player... players) {
        final var futures = Arrays.stream(players)
                .filter(Objects::nonNull)
                        .map( player -> {
                    final var instanceMessage = Component.text(player.getUsername() + " joined the session", NamedTextColor.GREEN);
                    final var playerMessage = Component.text("Teleporting you to: " + presentation.algorithmId(), NamedTextColor.GREEN);
                    // broadcast to all players in the instance
                    this.audience.sendMessage(instanceMessage);
                    // privately message player
                    player.sendMessage(playerMessage);
                    // add player to audience
                    this.audience.addAudience(player);
                    return player.setInstance(this.instance).thenCompose(_ -> player.teleport(origin));
                })
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    public boolean isEmpty() {
        return audience.isEmpty();
    }

    public CompletableFuture<Void> removePlayer(@NotNull Instance returnInstance, Player... players) {
        final var instance = Objects.requireNonNull(returnInstance, "Tried to send player to null returnInstance");

        if (instance == this.instance) {
            System.err.println("Tried to remove player from the instance they are already in");
            this.audience.sendMessage(Component.text("An error occurred", NamedTextColor.RED));
            return CompletableFuture.completedFuture(null);
        }

        final var future = Arrays.stream(players).map(player -> {
            this.audience.removeAudience(player);
            this.audience.sendMessage(Component.text(player.getUsername() + " left the session", NamedTextColor.RED));
            return player.setInstance(instance).thenCompose(_ -> player.teleport(new Pos(194.5, 137, -38.5)));
        }).toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(future)
                .thenRun(() -> {
                    // health check to make sure the instance is empty before unregistering it
                    if (this.audience.isEmpty()) MinecraftServer.getInstanceManager().unregisterInstance(this.instance);
                });
    }

    public CompletableFuture<Void> startVisualization() {
        return controller.startVisualization();
    }
    public AlgorithmPresentation getPresentation() {
        return this.presentation;
    }

    public PlayerControls getController() {
        return this.controller;
    }
}
