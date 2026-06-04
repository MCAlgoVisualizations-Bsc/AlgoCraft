package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AnimationPlan<O extends ISceneOps> {

    /**
     * A single scheduled operation within an animation plan.
     *
     * @param ticks wait time after the step completes
     * @param op the scene operation to run
     */
    public record Step<O extends ISceneOps>(
            int ticks,
            Function<O, CompletableFuture<Void>> op
    ) {
        public Step {
            if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
            Objects.requireNonNull(op, "op");
        }

        /**
         * Executes the step against the provided scene.
         *
         * @param scene the scene to apply the step to
         * @return a future that completes when the step finishes
         */
        public CompletableFuture<Void> run(O scene) {
            return op.apply(scene);
        }
    }

    private final List<Step<O>> steps;

    private AnimationPlan(List<Step<O>> steps) {
        this.steps = List.copyOf(steps);
    }

    /**
     * Returns the immutable list of steps contained in this plan.
     *
     * @return plan steps
     */
    public List<Step<O>> steps() {
        return steps;
    }

    /**
     * Returns whether this plan contains any steps.
     *
     * @return {@code true} if the plan is empty
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * Creates a new animation plan builder.
     *
     * @param <O> scene type
     * @return a new builder
     */
    public static <O extends ISceneOps> Builder<O> builder() {
        return new Builder<>();
    }

    /**
     * Creates a one-step plan from a synchronous scene operation.
     *
     * @param op the operation to run
     * @param <O> scene type
     * @return a one-step plan
     */
    public static <O extends ISceneOps> AnimationPlan<O> instant(Consumer<O> op) {
        return AnimationPlan.<O>builder()
                .step(op)
                .build();
    }

    /**
     * Creates a one-step plan from an asynchronous scene operation.
     *
     * @param op the operation to run
     * @param <O> scene type
     * @return a one-step plan
     */
    public static <O extends ISceneOps> AnimationPlan<O> instantAsync(
            Function<O, CompletableFuture<Void>> op
    ) {
        return AnimationPlan.<O>builder()
                .stepAsync(op)
                .build();
    }

    /**
     * Returns an empty animation plan.
     *
     * @param <O> scene type
     * @return an empty plan
     */
    public static <O extends ISceneOps> AnimationPlan<O> empty() {
        return new AnimationPlan<>(Collections.emptyList());
    }

    public static final class Builder<O extends ISceneOps> {
        private final List<Step<O>> steps = new ArrayList<>();

        /**
         * Adds a no-op step with the given delay.
         */
        public Builder<O> step(int ticks) {
            return step(ticks, _ -> {});
        }

        /**
         * Adds a one-tick synchronous step.
         */
        public Builder<O> step(Consumer<O> op) {
            return step(1, op);
        }

        /**
         * Adds a synchronous step.
         */
        public Builder<O> step(int ticks, Consumer<O> op) {
            Objects.requireNonNull(op, "op");

            steps.add(new Step<>(ticks, scene -> {
                op.accept(scene);
                return CompletableFuture.completedFuture(null);
            }));

            return this;
        }

        /**
         * Adds a one-tick asynchronous step.
         */
        public Builder<O> stepAsync(Function<O, CompletableFuture<Void>> op) {
            return stepAsync(1, op);
        }

        /**
         * Adds an asynchronous step.
         */
        public Builder<O> stepAsync(int ticks, Function<O, CompletableFuture<Void>> op) {
            Objects.requireNonNull(op, "op");

            steps.add(new Step<>(ticks, scene -> {
                CompletableFuture<Void> future = op.apply(scene);
                return future == null
                        ? CompletableFuture.completedFuture(null)
                        : future;
            }));

            return this;
        }

        /**
         * Builds the plan.
         *
         * @return the immutable animation plan
         */
        public AnimationPlan<O> build() {
            return new AnimationPlan<>(steps);
        }
    }
}