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
import net.kyori.adventure.audience.Audience;
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
    private Instance instance;
    private final AlgorithmPresentation presentation;
    private final PlayerFeedback audience;
    private final VisualizationController<T, C> controller;

    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Audience... audiences) {
        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.presentation = algorithm.presentation();
        this.audience = new PlayerFeedback(audiences);

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
    }

    public Instance getInstance() {
        return this.instance;
    }

    public UUID getUUID() {
        return this.instance.getUuid();
    }

    public void addPlayer(Player... players) {
        Arrays.stream(players).forEach( player -> {
            final var message = Component.text(player.getUsername() + " joined the session", NamedTextColor.GREEN);
            this.instance.sendMessage(message); // broadcast to all players
            player.setInstance(this.instance);
        });
    }

    public void removePlayer(@NotNull Instance returnInstance, Player... players) {
        final var instance = Objects.requireNonNull(returnInstance, "Tried to send player to null instance");
        if (instance == this.instance)
            System.err.println("Tried to remove player from the instance they are already in");
        Arrays.stream(players).forEach( player -> {
            final var message = Component.text(player.getUsername() + " left the session", NamedTextColor.RED);
            this.instance.sendMessage(message); // broadcast to all players
            player.setInstance(instance);
        });
        if (this.instance.getPlayers().isEmpty()) {
            clear();
        }
    }

    private void clear() {
        this.instance = null;
        this.controller.clear();
    }

    public AlgorithmPresentation getPresentation() {
        return this.presentation;
    }

    public VisualizationController<T, C> getController() {
        return this.controller;
    }
}
