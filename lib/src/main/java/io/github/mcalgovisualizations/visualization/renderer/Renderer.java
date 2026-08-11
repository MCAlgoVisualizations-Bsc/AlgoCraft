package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.instance.AudienceChannel;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.Dispatcher;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class Renderer<I, O extends ISceneOps> {
    private final O scene;
    private final Instance instance;
    private final Dispatcher<O> dispatcher;
    private final AnimationPlan<O> complete;
    private final Pos origin;
    private final ILayout<I> layout;
    private boolean collapseAnimationDelays = false;
    private Executor<O> executor;

    /**
     * Creates a renderer for a model/layout pair and a specific runtime scene.
     */
    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout<I> layout,
            @NotNull AudienceChannel audience,
            @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers,
            @NotNull AnimationPlan<O> complete,
            @NotNull O scene
    ) {
        this.instance = instance;
        this.origin = origin;
        this.scene = scene;
        this.layout = layout;
        this.dispatcher = new Dispatcher<>(handlers);
        this.complete = complete;
        this.executor = new Executor<>(scene);
    }

    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout<I> layout,
            @NotNull AudienceChannel audience,
            @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers,
            @NotNull AnimationPlan<O> complete,
            @NotNull O scene,
            @NotNull Executor<O> executor
    ) {
        this(instance, origin, layout, audience, handlers, complete, scene);
        this.executor = executor;
    }

    /**
     * Updates the animation playback speed.
     *
     * @param ticksPerStep ticks between scheduled steps
     */
    public void setSpeed(int ticksPerStep) {
        this.collapseAnimationDelays = ticksPerStep <= 1;
        executor.setSpeed(ticksPerStep);
    }

    /**
     * Pauses queued animation playback.
     */
    public void pause() {
        executor.pause();
    }

    /**
     * Resumes queued animation playback.
     */
    public void resume() {
        executor.resume();
    }

    /**
     * Dispatches a single algorithm event into the scene.
     *
     * @param event the event to render
     */
    public void render(IAlgorithmEvent event) {
        if (event == null) {
            System.err.println("Received null event");
            return;
        }

        try {
            final var plan = normalizePlan(dispatcher.dispatch(event));
            executor.add(plan);
        } catch (IllegalStateException e) {
            System.err.println("Error while dispatching event: " + e.getMessage());
        } finally {
            executor.startIfIdle();
        }
    }

    /**
     * Enqueues the algorithm completion animation.
     */
    public void complete() {
        executor.add(normalizePlan(complete));
        executor.startIfIdle();
    }

    /**
     * Returns whether the executor still has queued or active animation work.
     *
     * @return {@code true} if animations are still pending
     */
    public boolean hasPendingAnimations() {
        return !executor.isIdle();
    }

    /**
     * Releases the scene and cancels any pending playback state.
     */
    public void onCleanup() {
        executor.onCleanup();
        scene.cleanUp();
    }

    /**
     * Computes the initial layout and spawns all displays into the scene.
     *
     * @param initialModel the initial model snapshot
     * @return a future that completes once chunk loading and layout setup finish
     */
    public CompletableFuture<Void> initialize(I initialModel) {
        Objects.requireNonNull(instance, "Renderer.instance is null");
        final var layoutResults = Objects.requireNonNull(
                this.layout.compute(initialModel, origin, instance),
                "layout.compute returned null"
        );

        final var futures = Arrays.stream(layoutResults)
                .filter(Objects::nonNull)
                .map(key -> instance.loadChunk(key.pos().chunkX(), key.pos().chunkZ()))
                .toArray(CompletableFuture<?>[]::new);

        return CompletableFuture.allOf(futures)
                .thenRun(() -> scene.setLayout(layoutResults));
    }

    private AnimationPlan<O> normalizePlan(AnimationPlan<O> plan) {
        if (!collapseAnimationDelays || plan.isEmpty()) {
            return plan;
        }

        var builder = AnimationPlan.<O>builder();

        for (var step : plan.steps()) {
            builder.stepAsync(0, step.op());
        }

        return builder.build();
    }
}