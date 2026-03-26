package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.handlers.CompleteHandler;
import io.github.mcalgovisualizations.visualization.renderer.handlers.IAnimationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Algorithm<T extends Comparable<T>>(
    String id,
    Supplier<? extends IPlayerSort> ctor,
    List<Data<T>> lst,
    ILayout layout,
    AlgorithmPlacement placement,
    Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlerRegistry
) {
    public static <T extends Comparable<T>> Algorithm<T> register(Consumer<Builder<T>> configurer) {
        Builder<T> builder = new Builder<>();
        configurer.accept(builder);
        return builder.build();
    }

    public static final class Builder<T extends Comparable<T>> {
        private String id;
        private Supplier<? extends IPlayerSort> ctor;
        private List<Data<T>> data;
        private ILayout layout;
        private AlgorithmPlacement placement;
        private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();

        @SuppressWarnings("UnusedReturnValue")
        public Builder<T> identify(String id, Supplier<? extends IPlayerSort> ctor) {
            this.id = id;
            this.ctor = ctor;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder<T> withData(List<Data<T>> data) {
            this.data = List.copyOf(data);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder<T> positioning(ILayout layout, AlgorithmPlacement placement) {
            Objects.requireNonNull(layout, "layout");
            Objects.requireNonNull(placement, "placement");
            this.layout = layout;
            this.placement = placement;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder<T> onEvent(@NotNull Class<? extends IAlgorithmEvent> event, @NotNull IAnimationHandler<?> handler) {
            Objects.requireNonNull(event, "event");
            Objects.requireNonNull(handler, "handlerRegistry");
            this.handlers.put(event, handler);
            return this;
        }

        private Algorithm<T> build() {
            if (id == null || ctor == null || data == null || layout == null || placement == null) {
                throw new IllegalStateException("Missing mandatory algorithm configuration!");
            }
            if (handlers.isEmpty()) {
                throw new IllegalStateException("Missing event at least 1 event!");
            }
            if (!handlers.containsKey(Complete.class))
                handlers.put(Complete.class, new CompleteHandler());
            return new Algorithm<>(id, ctor, data, layout, placement, handlers);
        }
    }

}
