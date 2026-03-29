package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.Dispatcher;
import io.github.mcalgovisualizations.visualization.renderer.handlers.IAnimationHandler;
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

    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout layout,
            @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers,
            @NotNull AudienceChannel audience
    ) {
        this.instance = instance;
        this.scene = new Scene(instance, origin, audience);
        this.layout = layout;
        this.origin = origin;
        this.executor = new Executor(scene);
        this.dispatcher = new Dispatcher(handlers);
    }

    /**
     * Stop animation activity but keep the scene alive so you can resume.
     * Typical use: controller.pause().
     */
    public void stop() {
        executor.pause();
    }

    public void resume() {
        executor.resume();
    }

    public void render(IAlgorithmEvent event) {
        final var plan = dispatcher.dispatch(event);
        executor.add(plan);
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
