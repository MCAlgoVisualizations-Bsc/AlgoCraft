package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.algorithm.ContextFactory;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public record Algorithm<T, C extends AlgorithmContext<T>, O extends ISceneOps>(
        @NotNull String id,
        @NotNull Supplier<? extends IPlayerSort<C>> ctor,
        @NotNull C model,
        @NotNull ILayout<T> layout,
        @NotNull AlgorithmPlacement placement,
        @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlerRegistry,
        @NotNull Function<C, ? extends AnimationPlan<O>> onComplete,
        @Nullable AlgorithmPresentation presentation,
        @NotNull Function<SceneContext, O> scene,
        @NotNull ContextFactory<T, C> contextFactory
) {
    public Algorithm {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ctor, "ctor");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(placement, "placement");
        Objects.requireNonNull(handlerRegistry, "handlerRegistry");
        Objects.requireNonNull(onComplete, "onComplete");
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(contextFactory, "contextFactory");
        handlerRegistry = Map.copyOf(handlerRegistry);
    }

    public static <T, C extends AlgorithmContext<T>, O extends ISceneOps>
    @NotNull Builder<T, C, O> builder(@NotNull C model) {
        return new Builder<>(model);
    }

    public static final class Builder<T, C extends AlgorithmContext<T>, O extends ISceneOps> {

        private final C model;
        private final Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = new HashMap<>();

        private String id;
        private Supplier<? extends IPlayerSort<C>> ctor;
        private ILayout<T> layout;
        private AlgorithmPlacement placement;
        private Function<C, ? extends AnimationPlan<O>> onComplete;
        private @Nullable AlgorithmPresentation presentation;
        private Function<SceneContext, O> scene;
        private ContextFactory<T, C> contextFactory;

        private Builder(@NotNull C model) {
            this.model = Objects.requireNonNull(model, "model");
        }

        public @NotNull Builder<T, C, O> withIdentity(
                @NotNull String id,
                @NotNull Supplier<? extends IPlayerSort<C>> ctor
        ) {
            this.id = Objects.requireNonNull(id, "id");
            this.ctor = Objects.requireNonNull(ctor, "ctor");
            return this;
        }

        public @NotNull Builder<T, C, O> positioning(
                @NotNull ILayout<T> layout,
                @NotNull AlgorithmPlacement placement
        ) {
            this.layout = Objects.requireNonNull(layout, "layout");
            this.placement = Objects.requireNonNull(placement, "placement");
            return this;
        }

        public @NotNull Builder<T, C, O> withContextFactory(
                @NotNull ContextFactory<T, C> contextFactory
        ) {
            this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
            return this;
        }

        public @NotNull Builder<T, C, O> withContextFactory(
                @NotNull Function<T, C> contextCreator,
                @NotNull UnaryOperator<T> copier,
                @NotNull UnaryOperator<T> randomizer
        ) {
            this.contextFactory = new ContextFactory<>(
                    Objects.requireNonNull(contextCreator, "contextCreator"),
                    Objects.requireNonNull(copier, "copier"),
                    Objects.requireNonNull(randomizer, "randomizer")
            );
            return this;
        }

        @SuppressWarnings("unchecked")
        public <NO extends ISceneOps> @NotNull Builder<T, C, NO> withScene(
                @NotNull Function<SceneContext, NO> scene
        ) {
            this.scene = (Function<SceneContext, O>) Objects.requireNonNull(scene, "scene");
            return (Builder<T, C, NO>) this;
        }

        public <E extends IAlgorithmEvent> @NotNull Builder<T, C, O> onEvent(
                @NotNull Class<E> event,
                @NotNull IAnimationHandler<E> handler
        ) {
            handlers.put(
                    Objects.requireNonNull(event, "event"),
                    Objects.requireNonNull(handler, "handler")
            );
            return this;
        }

        public @NotNull Builder<T, C, O> onCompletion(
                @NotNull Function<C, ? extends AnimationPlan<O>> handler
        ) {
            this.onComplete = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public @NotNull Builder<T, C, O> withPresentation(
                @Nullable AlgorithmPresentation presentation
        ) {
            this.presentation = presentation;
            return this;
        }

        public @NotNull Algorithm<T, C, O> create() {
            if (handlers.isEmpty()) {
                throw new IllegalStateException("Missing at least 1 event handler");
            }

            return new Algorithm<>(
                    Objects.requireNonNull(id, "id"),
                    Objects.requireNonNull(ctor, "ctor"),
                    model,
                    Objects.requireNonNull(layout, "layout"),
                    Objects.requireNonNull(placement, "placement"),
                    handlers,
                    onComplete == null ? defaultOnComplete() : onComplete,
                    presentation,
                    Objects.requireNonNull(scene, "scene"),
                    Objects.requireNonNull(contextFactory, "contextFactory")
            );
        }

        private static <C extends AlgorithmContext<?>, O extends ISceneOps>
        @NotNull Function<C, AnimationPlan<O>> defaultOnComplete() {
            return ignored -> AnimationPlan.<O>builder()
                    .step(sceneOps -> sceneOps.sendMessage(Component.text(
                            "Algorithm is complete, click on randomize or step through the steps!",
                            NamedTextColor.GREEN
                    )))
                    .build();
        }
    }
}