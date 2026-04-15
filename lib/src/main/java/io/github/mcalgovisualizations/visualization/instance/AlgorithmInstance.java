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

public class AlgorithmInstance<T, C extends AlgorithmContext<T>, O extends ISceneOps> {
    private final Instance instance;
    private final AlgorithmPresentation presentation;
    private final PlayerFeedback audience;
    private final VisualizationController<T, C> controller;
    public static final Pos origin = new Pos(0, 40, 0);

    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Player... players) {
        var container = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.instance = container;
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

        container.setChunkSupplier(LightingChunk::new);

        //container.setChunkLoader(new AnvilLoader(worldPath));

        final var algorithmCtx = algorithm.model();
        final var a = algorithm.ctor().get();
        a.run(algorithmCtx);
        final AnimationPlan<O> onCompletePlan = algorithm.onComplete().apply(algorithmCtx);

        final var sceneCtx = new SceneContext(instance, audience, algorithm.placement().renderOrigin());
        final var scene = algorithm.scene().apply(sceneCtx);

        final var renderer = new Renderer<>(
                instance,
                algorithm.placement().renderOrigin(),
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

        // teleport all players to the instance
        // Arrays.stream(players).forEach(player -> player.setInstance(instance));
    }

    public Instance getInstance() {
        return this.instance;
    }

    public UUID getUuid() {
        return this.instance.getUuid();
    }

    public boolean containsPlayer(Player... players) {
        return this.audience.containsPlayer(players);
    }

    public void addPlayer(Player... players) {
        Arrays.stream(players).forEach( player -> {
            // add the player to the audience so they get notified when they join
            this.audience.addAudience(player);
            final var message = Component.text(player.getUsername() + " joined the session", NamedTextColor.GREEN);
            this.audience.sendMessage(message);
            player.setInstance(this.instance);
        });
    }

    public boolean isEmpty() {
        return audience.isEmpty();
    }

    public void removePlayer(@NotNull Instance returnInstance, Player... players) {
        final var instance = Objects.requireNonNull(returnInstance, "Tried to send player to null returnInstance");
        if (instance == this.instance) {
            System.err.println("Tried to remove player from the instance they are already in");
            this.audience.sendMessage(Component.text("An error occurred", NamedTextColor.RED));
            return;
        }
        for (var player : players) {
            if (player == null) continue;

            this.audience.removeAudience(player);
            this.audience.sendMessage(Component.text(player.getUsername() + " left the session", NamedTextColor.RED));
            player.setInstance(instance);
        }
    }

    public void teleportPlayer(Player... players) {
        for (var player : players) {
            player.setInstance(this.instance);
        }
    }

    public void startVisualization() {
        controller.startVisualization();
    }
    public AlgorithmPresentation getPresentation() {
        return this.presentation;
    }

    public PlayerControls getController() {
        return this.controller;
    }
}
