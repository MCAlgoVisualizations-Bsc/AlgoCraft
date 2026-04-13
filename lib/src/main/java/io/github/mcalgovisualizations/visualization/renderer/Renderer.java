package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.Dispatcher;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public final class Renderer<I, O extends ISceneOps> {
    private final O scene;
    private Instance instance;
    private final Dispatcher<O> dispatcher;
    private final Executor<O> executor;
    private final AnimationPlan<O> complete;
    private final Pos origin;
    private final ILayout<I> layout;
    private boolean collapseAnimationDelays = false;

    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout<I> layout,
            @NotNull AudienceChannel audience,
            @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers,
            @NotNull AnimationPlan<O> complete,
            @NotNull O scene
    ) {
        this.scene = scene;
        this.executor = new Executor<>(scene);
        this.dispatcher = new Dispatcher<>(handlers);
        this.complete = complete;

        this.instance = instance;
        this.layout = layout;
        this.origin = origin;
    }

    public void setSpeed(int ticksPerStep) {
        this.collapseAnimationDelays = ticksPerStep <= 1;
        executor.setSpeed(ticksPerStep);
    }

    /**
     * Stop animation activity but keep the scene alive so you can resume.
     * Typical use: controller.pause().
     */
    public void pause() {
        executor.pause();
    }

    public void resume() {
        executor.resume();
    }

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

    public void complete() {
        executor.add(normalizePlan(complete));
        executor.startIfIdle();
    }

    public boolean hasPendingAnimations() {
        return !executor.isIdle();
    }

    /**
     * Full teardown. Not resumable.
     * Typical use: application shutdown / leaving visualization.
     */
    public void onCleanup() {
        executor.onCleanup();   // kill tick loop + clear queue
        scene.cleanUp();   // despawn entities
        instance = null;
    }

    public void initialize(I initialModel) {
        final var layoutResult = this.layout.compute(initialModel, origin, instance);
        requireChunksLoaded(layoutResult);
        scene.setLayout(layoutResult);
    }

    private void requireChunksLoaded(LayoutResult[] layoutResult) {
        final var allLoaded = Arrays.stream(layoutResult)
                .allMatch(r -> instance.isChunkLoaded(r.pos().chunkX(), r.pos().chunkZ()));

        if (!allLoaded) {
            throw new IllegalStateException("Visualization area is not loaded yet.");
        }
    }

    private AnimationPlan<O> normalizePlan(AnimationPlan<O> plan) {
        if (!collapseAnimationDelays || plan.isEmpty()) {
            return plan;
        }

        AnimationPlan.Builder<O> builder = AnimationPlan.builder();
        for (var step : plan.steps()) {
            builder.step(0, step.op());
        }
        return builder.build();
    }

}
