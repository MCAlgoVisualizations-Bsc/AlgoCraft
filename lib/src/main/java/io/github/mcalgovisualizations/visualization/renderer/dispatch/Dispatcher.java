package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class Dispatcher<O extends ISceneOps> {

    private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers;

    /**
     * Creates a dispatcher backed by the provided handler registry.
     *
     * @param handlers concrete event-to-handler mappings
     */
    public Dispatcher(Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers) {
        this.handlers = Map.copyOf(handlers);
    }

    /**
     * Dispatches an event to its registered animation handler.
     *
     * @param event the concrete event to dispatch
     * @return the resulting animation plan
     * @throws IllegalStateException if no handler exists for the event class
     */
    public AnimationPlan<O> dispatch(@NotNull IAlgorithmEvent event) {
        Objects.requireNonNull(event, "event");
        final var handler = handlers.get(event.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for event type " + event.getClass().getName());
        }

        return invokeUnchecked(handler, event);
    }

    @SuppressWarnings("unchecked")
    private <E extends IAlgorithmEvent> AnimationPlan<O> invokeUnchecked(
            @NotNull IAnimationHandler<?> raw,
            @NotNull IAlgorithmEvent event
    ) {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(event, "event");
        return ((IAnimationHandler<E>) raw).handle((E) event);
    }
}

