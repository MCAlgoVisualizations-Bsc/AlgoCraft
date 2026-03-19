package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.algorithms.events.*;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.Dispatcher;
import io.github.mcalgovisualizations.visualization.renderer.handlers.*;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Renderer {
    private final Scene scene;
    private final Dispatcher dispatcher = new Dispatcher();
    private final Executor executor;
    private final Pos origin;
    private final ILayout layout;
    private boolean started = false;

    public Renderer(
            @NotNull Instance instance,
            @NotNull Pos origin,
            @NotNull ILayout layout
    ) {
        this.scene = new Scene(instance, origin);
        this.layout = layout;
        this.origin = origin;
        this.executor = new Executor(scene);
    }

    /**
     * Stop animation activity but keep the scene alive so you can resume.
     * Typical use: controller.pause().
     */
    public void Stop() {
        if (!started) return;
        executor.pause();
    }

    public void Resume() {
        if (!started) return;
        executor.resume();
    }

    public void setAudience(Audience audience) {
        scene.setAudience(audience);
    }


    public void render(IAlgorithmEvent event) {
        final var plan = dispatcher.dispatch(event);
        executor.add(plan);
        executor.startIfIdle();
    }

    /**
     * Full teardown. Not resumable.
     * Typical use: application shutdown / leaving visualization.
     */
    public void onCleanup() {
        if (!started) return;
        executor.onCleanup();   // kill tick loop + clear queue
        scene.cleanUp();   // despawn entities
        started = false;
    }

    public <T extends Comparable<T>> void initialize(List<Data<T>> initialModel) {
        if (started) return;
        started = true;
        dispatcher.register(Compare.class, new CompareHandler());
        dispatcher.register(Swap.class, new SwapHandler());
        dispatcher.register(Complete.class, new CompleteHandler());
        dispatcher.register(Message.class, new MessageHandler());
        dispatcher.register(Validate.class, new ValidateHandler());
        dispatcher.register(NoOp.class, new NoOpHandler());

        final var layoutResult = this.layout.compute(initialModel, origin);
        scene.onStart(layoutResult);
    }

}
