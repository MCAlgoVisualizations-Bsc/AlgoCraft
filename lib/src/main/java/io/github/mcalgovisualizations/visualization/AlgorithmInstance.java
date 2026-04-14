package io.github.mcalgovisualizations.visualization;

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
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class AlgorithmInstance<T, C extends AlgorithmContext<T>, O extends ISceneOps> {
    private final Instance instance;
    private final AlgorithmPresentation presentation;
    private final PlayerFeedback audience;
    private final VisualizationController<T, C> controller;

    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Player... players) {
        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.presentation = algorithm.presentation();
        this.audience = new PlayerFeedback(players);

        final var algorithmCtx = algorithm.contextFactory().create(algorithm.model().getData());
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
                algorithm.model().getData(),
                algorithm.contextFactory()
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

    public AlgorithmPresentation getPresentation() {
        return this.presentation;
    }

    public PlayerControls getController() {
        return this.controller;
    }
}
