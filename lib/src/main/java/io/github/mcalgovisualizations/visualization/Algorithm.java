package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.*;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Algorithm<I, C extends AlgorithmContext<I>, O extends ISceneOps>(
        @NotNull String id,
        @NotNull Supplier<? extends IPlayerSort<C>> ctor,
        @NotNull C context,
        @NotNull ILayout<I> layout,
        @NotNull AlgorithmPlacement placement,
        @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlerRegistry,
        @NotNull Function<C, ? extends AnimationPlan<O>> onComplete,
        @Nullable AlgorithmPresentation presentation,
        @NotNull Function<SceneContext, O> scene
) {
    public static @NotNull <I, C extends AlgorithmContext<I>, O extends ISceneOps> Algorithm<I, C, O> build(
            @NotNull Consumer<Builder<I, C, O>> configurer
    ) {
        var builder = new Builder<I, C, O>();
        configurer.accept(builder);
        return builder.create();
    }

    public static final class Builder<I, C extends AlgorithmContext<I>, O extends ISceneOps> {
        private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();
        private Function<C, ? extends AnimationPlan<O>> onComplete;
        private Function<SceneContext, O> scene;

        private String id;
        private Supplier<? extends IPlayerSort<C>> ctor;
        private C model;
        private ILayout<I> layout;
        private AlgorithmPlacement placement;
        private @Nullable AlgorithmPresentation presentation;

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<I, C, O> withIdentity(
                @NotNull String id,
                @NotNull Supplier<? extends IPlayerSort<C>> ctor
        ) {
            this.id = Objects.requireNonNull(id, "id");
            this.ctor = Objects.requireNonNull(ctor, "ctor");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<I, C, O> withScene(@NotNull Function<SceneContext, O> scene) {
            this.scene = Objects.requireNonNull(scene, "scene");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<I, C, O> withContext(@NotNull C model) {
            this.model = Objects.requireNonNull(model, "model");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<I, C, O> positioning(
                @NotNull ILayout<I> layout,
                @NotNull AlgorithmPlacement placement
        ) {
            this.layout = Objects.requireNonNull(layout, "layout");
            this.placement = Objects.requireNonNull(placement, "placement");
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public <E extends IAlgorithmEvent> @NotNull Builder<I, C, O> onEvent(
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
        public @NotNull Builder<I, C, O> onCompletion(
                @NotNull Function<C, ? extends AnimationPlan<O>> handler
        ) {
            this.onComplete = Objects.requireNonNull(handler, "handler");
            return this;
        }



        @SuppressWarnings("UnusedReturnValue")
        public @NotNull Builder<I, C, O> withPresentation(
                @Nullable AlgorithmPresentation presentation
        ) {
            this.presentation = presentation;
            return this;
        }

        private @NotNull Algorithm<I, C, O> create() {
            if (handlers.isEmpty()) {
                throw new IllegalStateException("Missing at least 1 event handler");
            }

            return new Algorithm<>(
                    Objects.requireNonNull(id, "id"),
                    Objects.requireNonNull(ctor, "ctor"),
                    Objects.requireNonNull(model, "model"),
                    Objects.requireNonNull(layout, "layout"),
                    Objects.requireNonNull(placement, "placement"),
                    Map.copyOf(handlers),
                    onComplete == null ? defaultOnComplete() : onComplete,
                    presentation,
                    Objects.requireNonNull(scene, "scene")
            );
        }

        private static <C extends AlgorithmContext<?>, O extends ISceneOps>
        Function<C, AnimationPlan<O>> defaultOnComplete() {
            return _ -> {
                var plan = AnimationPlan.<O>builder()
                        .step(sceneOps -> {
                            var component = Component.text(
                                    "Algorithm is complete, click on randomize or step through the steps!",
                                    NamedTextColor.GREEN
                            );
                            sceneOps.sendMessage(component);
                        });

//                for (int i = 0; i < size; i++) {
//                    final int idx = i;
//                    plan.step(sceneOps -> sceneOps.hoverDisplay(idx, true));
//                }
//
//                for (int i = 0; i < size; i++) {
//                    final int idx = i;
//                    plan.step(sceneOps -> sceneOps.hoverDisplay(idx, false));
//                }

                return plan.build();
            };
        }
    }
}