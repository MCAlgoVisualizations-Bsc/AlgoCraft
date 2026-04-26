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
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AlgorithmInstance<T, C extends AlgorithmContext<T>, O extends ISceneOps> {
    private final Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
    private final AlgorithmPresentation presentation;
    private final PartyService partyService;
    private final VisualizationController<T, C> controller;
    public static final Pos INSTANCE_ORIGIN = new Pos(0, 40, 0);

    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Player... players) {
        this.presentation = algorithm.presentation();
        this.partyService = new PartyService(players[0]);

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

        final var audience = new PlayerFeedback(partyService::audience);
        final var sceneCtx = new SceneContext(instance, audience, INSTANCE_ORIGIN);
        final var scene = algorithm.scene().apply(sceneCtx);

        final var renderer = new Renderer<>(
                instance,
                INSTANCE_ORIGIN,
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

    public CompletableFuture<Void> addPlayer(Player... players) {
        return partyService.addSpectator(instance, INSTANCE_ORIGIN, players);
    }

    public CompletableFuture<Void> removePlayer(@NotNull Instance returnInstance, Player... players) {
        return partyService.removeSpectator(returnInstance, AlgoCraft.SPAWN_POS, players);
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
