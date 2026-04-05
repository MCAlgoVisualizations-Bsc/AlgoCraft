package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.layouts.ILayout;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Algorithm<T extends Comparable<T>>(
        @NotNull String id,
        @NotNull Supplier<? extends IPlayerSort> ctor,
        @NotNull List<Data<T>> model,
        @NotNull ILayout layout,
        @NotNull AlgorithmPlacement placement,
        @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlerRegistry,
        @NotNull Function<? super ISort<T>, ? extends AnimationPlan> onComplete,
        @Nullable AlgorithmPresentation presentation
) {
    public static <T extends Comparable<T>> @NotNull Algorithm<T> build(
            @NotNull Consumer<Builder<T>> configurer
    ) {
        var builder = new Builder<T>();
        configurer.accept(builder);
        return builder.create();
    }

    public static final class Builder<T extends Comparable<T>> {
        private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();
        private Function<? super ISort<T>, ? extends AnimationPlan> onComplete;

        private String id;
        private Supplier<? extends IPlayerSort> ctor;
        private List<Data<T>> model;
        private ILayout layout;
        private AlgorithmPlacement placement;
        private @Nullable AlgorithmPresentation presentation;

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<T> withIdentity(
                @NotNull String id,
                @NotNull Supplier<? extends IPlayerSort> ctor
        ) {
            this.id = Objects.requireNonNull(id, "id");
            this.ctor = Objects.requireNonNull(ctor, "ctor");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<T> withData(@NotNull List<Data<T>> model) {
            this.model = List.copyOf(Objects.requireNonNull(model, "model"));
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<T> positioning(
                @NotNull ILayout layout,
                @NotNull AlgorithmPlacement placement
        ) {
            this.layout = Objects.requireNonNull(layout, "layout");
            this.placement = Objects.requireNonNull(placement, "placement");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public <E extends IAlgorithmEvent> @NotNull Builder<T> onEvent(
                @NotNull Class<E> event,
                @NotNull IAnimationHandler<E> handler
        ) {
            this.handlers.put(
                    Objects.requireNonNull(event, "event"),
                    Objects.requireNonNull(handler, "handler")
            );
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<T> onCompletion(
                @NotNull Function<? super ISort<T>, ? extends AnimationPlan> handler
        ) {
            this.onComplete = Objects.requireNonNull(handler, "handler");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<T> withPresentation(
               @Nullable AlgorithmPresentation presentation
        ) {
            this.presentation = presentation;
            return this;
        }

        private @NotNull Algorithm<T> create() {
            if (handlers.isEmpty()) {
                throw new IllegalStateException("Missing at least 1 event handler");
            }

            return new Algorithm<T>(
                    Objects.requireNonNull(id, "id"),
                    Objects.requireNonNull(ctor, "ctor"),
                    Objects.requireNonNull(model, "model"),
                    Objects.requireNonNull(layout, "layout"),
                    Objects.requireNonNull(placement, "placement"),
                    Map.copyOf(handlers),
                    onComplete == null ? defaultOnComplete() : onComplete,
                    presentation
            );
        }

        private static <T extends Comparable<T>>
        Function<? super ISort<T>, ? extends AnimationPlan> defaultOnComplete() {
            return collection -> {
                var size = collection.copy().size();

                var plan = AnimationPlan.builder()
                        .step(ISceneOps::stopAnimations)
                        .step(sceneOps -> {
                            var component = Component.text(
                                    "Algorithm is complete, click on randomize or step through the steps!",
                                    NamedTextColor.GREEN
                            );
                            sceneOps.sendMessage(component);
                        });

                for (int i = 0; i < size; i++) {
                    final int idx = i;
                    plan.step(sceneOps -> sceneOps.hoverDisplay(idx, true));
                }

                for (int i = 0; i < size; i++) {
                    final int idx = i;
                    plan.step(sceneOps -> sceneOps.hoverDisplay(idx, false));
                }

                return plan.build();
            };
        }
    }
}