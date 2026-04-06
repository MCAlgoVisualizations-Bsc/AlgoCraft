package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class Dispatcher<O extends ISceneOps> {

    private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers;

    public Dispatcher(Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers) {
        this.handlers = Map.copyOf(handlers);
    }

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

