package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.Dispatcher;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public final class Renderer {
    private final Scene scene;
    private final Instance instance;
    private final Dispatcher dispatcher;
    private final Executor executor;
    private final Pos origin;
    private final ILayout layout;
    private final AnimationPlan complete;

    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout layout,
            @NotNull AudienceChannel audience,
            @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers,
            @NotNull AnimationPlan complete
    ) {
        this.scene = new Scene(instance, origin, audience);
        this.executor = new Executor(scene);
        this.dispatcher = new Dispatcher(handlers);
        this.complete = complete;

        // make these into context?
        this.instance = instance;
        this.layout = layout;
        this.origin = origin;
    }

    public void setSpeed(int ticksPerStep) {
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
            final var plan = dispatcher.dispatch(event);
            executor.add(plan);
        } catch (IllegalStateException e) {
            System.err.println("Error while dispatching event: " + e.getMessage());
        } finally {
            executor.startIfIdle();
        }
    }

    public void complete() {
        executor.add(complete);
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
    }

    public <T extends Comparable<T>> void initialize(List<Data<T>> initialModel) {
        final var layoutResult = this.layout.compute(initialModel, origin, instance);
        scene.setLayout(layoutResult);
    }

}
