package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import io.github.mcalgovisualizations.visualization.ui.IAlgorithmUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public record Algorithm<T, C extends AlgorithmContext<T>, O extends ISceneOps>(
        @NotNull String id,
        @NotNull Supplier<? extends IPlayerSort<C>> ctor,
        @NotNull C model,
        @NotNull ILayout<T> layout,
        @NotNull Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlerRegistry,
        @NotNull Function<C, ? extends AnimationPlan<O>> onComplete,
        @Nullable AlgorithmPresentation presentation,
        @NotNull Function<SceneContext, O> scene,
        @NotNull IAlgorithmUI runningLayout,
        boolean supportsPOV
) {
    /**
     * Creates a validated algorithm record and copies the handler registry defensively.
     */
    public Algorithm {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ctor, "ctor");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(handlerRegistry, "handlerRegistry");
        Objects.requireNonNull(onComplete, "onComplete");
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(runningLayout, "runningLayout");
        handlerRegistry = Map.copyOf(handlerRegistry);
    }

    /**
     * Starts building a new algorithm definition for the given model.
     *
     * @param model the initial model snapshot used to build playback traces
     * @param <T> model value type
     * @param <C> context type
     * @param <O> scene type
     * @return a new builder for the algorithm
     */
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
        private Function<C, ? extends AnimationPlan<O>> onComplete;
        private @Nullable AlgorithmPresentation presentation;
        private Function<SceneContext, O> scene;
        private IAlgorithmUI runningLayout = new AlgorithmUI();
        private boolean supportsPOV = false;

        /**
         * Creates a builder seeded with the model to visualize.
         *
         * @param model the initial model snapshot
         */
        private Builder(@NotNull C model) {
            this.model = Objects.requireNonNull(model, "model");
        }

        /**
         * Sets the algorithm identity and constructor.
         *
         * @param id algorithm id shown in the UI
         * @param ctor constructor for the algorithm implementation
         * @return this builder
         */
        public @NotNull Builder<T, C, O> withIdentity(
                @NotNull String id,
                @NotNull Supplier<? extends IPlayerSort<C>> ctor
        ) {
            this.id = Objects.requireNonNull(id, "id");
            this.ctor = Objects.requireNonNull(ctor, "ctor");
            return this;
        }

        /**
         * Sets the layout strategy used to place model values in the world.
         *
         * @param layout layout implementation
         * @return this builder
         */
        public @NotNull Builder<T, C, O> positioning(
                @NotNull ILayout<T> layout
        ) {
            this.layout = Objects.requireNonNull(layout, "layout");
            return this;
        }


        @SuppressWarnings("unchecked")
        /**
         * Sets the scene factory used to create the runtime scene.
         *
         * @param scene scene factory
         * @param <NO> concrete scene type
         * @return this builder with the new scene type
         */
        public <NO extends ISceneOps> @NotNull Builder<T, C, NO> withScene(
                @NotNull Function<SceneContext, NO> scene
        ) {
            this.scene = (Function<SceneContext, O>) Objects.requireNonNull(scene, "scene");
            return (Builder<T, C, NO>) this;
        }

        /**
         * Registers an event handler for a concrete event class.
         *
         * @param event the event class to handle
         * @param handler the handler that converts the event into animation steps
         * @param <E> event type
         * @return this builder
         */
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

        /**
         * Sets the animation plan to run after the algorithm finishes.
         *
         * @param handler completion animation factory
         * @return this builder
         */
        public @NotNull Builder<T, C, O> onCompletion(
                @NotNull Function<C, ? extends AnimationPlan<O>> handler
        ) {
            this.onComplete = Objects.requireNonNull(handler, "handler");
            return this;
        }

        /**
         * Sets the UI presentation metadata for the algorithm.
         *
         * @param presentation selector presentation, or null for the default icon/name
         * @return this builder
         */
        public @NotNull Builder<T, C, O> withPresentation(
                @Nullable AlgorithmPresentation presentation
        ) {
            this.presentation = presentation;
            return this;
        }

        /**
         * Sets the running-state inventory layout.
         *
         * @param runningLayout inventory layout shown while the visualization is active
         * @return this builder
         */
        public @NotNull Builder<T, C, O> withRunningLayout(@NotNull IAlgorithmUI runningLayout) {
            this.runningLayout = Objects.requireNonNull(runningLayout, "runningLayout");
            return this;
        }

        /**
         * Enables or disables villager POV support for the algorithm.
         *
         * @param supportsPOV whether the algorithm exposes POV mode
         * @return this builder
         */
        public @NotNull Builder<T, C, O> withPOVSupport(boolean supportsPOV) {
            this.supportsPOV = supportsPOV;
            return this;
        }

        /**
         * Builds the immutable algorithm definition.
         *
         * @return the finished algorithm definition
         * @throws IllegalStateException if no event handlers were registered
         */
        public @NotNull Algorithm<T, C, O> create() {
            if (handlers.isEmpty()) {
                throw new IllegalStateException("Missing at least 1 event handler");
            }

            return new Algorithm<>(
                    Objects.requireNonNull(id, "id"),
                    Objects.requireNonNull(ctor, "ctor"),
                    model,
                    Objects.requireNonNull(layout, "layout"),
                    handlers,
                    onComplete == null ? defaultOnComplete() : onComplete,
                    presentation,
                    Objects.requireNonNull(scene, "scene"),
                    Objects.requireNonNull(runningLayout, "runningLayout"),
                    supportsPOV
            );
        }

        /**
         * Returns the default completion animation used when none is provided.
         */
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