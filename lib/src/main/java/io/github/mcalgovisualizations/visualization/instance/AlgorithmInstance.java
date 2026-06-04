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
    private final O scene;
    public static final Pos INSTANCE_ORIGIN = new Pos(0, 40, 0);

    /**
     * Creates a runtime session for a specific algorithm and player group.
     *
     * @param algorithm the algorithm definition to run
     * @param players the players that initially join the session
     */
    public AlgorithmInstance(Algorithm<T,C,O> algorithm, Player... players) {
        this.presentation = algorithm.presentation();
        this.partyService = new PartyService(players[0], algorithm.id());

        instance.setGenerator(unit -> {
            final Point start = unit.absoluteStart();
            final Point size = unit.size();
            for (int x = 0; x < size.blockX(); x++) {
                for (int z = 0; z < size.blockZ(); z++) {
                    for (int y = 0; y < Math.min(40 - start.blockY(), size.blockY()); y++) {
                        unit.modifier().setBlock(start.add(x, y, z), Block.GRASS_BLOCK);
                    }
                }
            }
        });

        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);
        instance.setTime(6000);

        //container.setChunkLoader(new AnvilLoader(worldPath));
        final var onCompleteCtx = ((C) algorithm.model().copy());
        final var completeAlgorithm = algorithm.ctor().get();
        completeAlgorithm.run(onCompleteCtx);
        final AnimationPlan<O> onCompletePlan = algorithm.onComplete().apply(onCompleteCtx);

        final var audience = new PlayerFeedback(partyService::audience);
        final var sceneCtx = new SceneContext(instance, audience, INSTANCE_ORIGIN);
        this.scene = algorithm.scene().apply(sceneCtx);

        final var renderer = new Renderer<>(
                instance,
                INSTANCE_ORIGIN,
                algorithm.layout(),
                audience,
                algorithm.handlerRegistry(),
                onCompletePlan,
                this.scene
        );

        final var traceBuilder = new AlgorithmTraceBuilder<>(
                algorithm.ctor().get(),
                algorithm.model()
        );

        this.controller = new VisualizationController<>(
                renderer,
                traceBuilder,
                audience
        );

    }

    /**
     * Returns the Minestom instance used to render the visualization.
     *
     * @return the backing instance
     */
    public Instance getInstance() {
        return this.instance;
    }

    /**
     * Adds one or more players as spectators to the active session.
     *
     * @param players players to add
     * @return a future that completes when the players are moved
     */
    public CompletableFuture<Void> addPlayer(Player... players) {
        return partyService.addSpectator(instance, INSTANCE_ORIGIN, players);
    }

    /**
     * Removes players from the session and returns them to another instance.
     *
     * @param returnInstance the instance to return players to
     * @param players players to remove
     * @return a future that completes when the players are moved
     */
    public CompletableFuture<Void> removePlayer(@NotNull Instance returnInstance, Player... players) {
        return partyService.removeSpectator(returnInstance, AlgoCraft.SPAWN_POS, players);
    }

    /**
     * Initializes playback for the visualization.
     *
     * @return a future that completes when the scene has been initialized
     */
    public CompletableFuture<Void> startVisualization() {
        return controller.startVisualization();
    }
    /**
     * Returns the presentation metadata used by the selector UI.
     *
     * @return the presentation metadata
     */
    public AlgorithmPresentation getPresentation() {
        return this.presentation;
    }

    /**
     * Returns the playback controller for the session.
     *
     * @return the controller instance
     */
    public PlayerControls getController() {
        return this.controller;
    }

    /**
     * Returns the runtime scene created for this session.
     *
     * @return the active scene
     */
    public O getScene() {
        return scene;
    }
}
