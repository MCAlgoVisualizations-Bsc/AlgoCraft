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

    public record Step<O extends ISceneOps>(
            int ticks,
            Function<O, CompletableFuture<Void>> op
    ) {
        public Step {
            if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
            Objects.requireNonNull(op, "op");
        }

        public CompletableFuture<Void> run(O scene) {
            return op.apply(scene);
        }
    }

    private final List<Step<O>> steps;

    private AnimationPlan(List<Step<O>> steps) {
        this.steps = List.copyOf(steps);
    }

    public List<Step<O>> steps() {
        return steps;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public static <O extends ISceneOps> Builder<O> builder() {
        return new Builder<>();
    }

    public static <O extends ISceneOps> AnimationPlan<O> instant(Consumer<O> op) {
        return AnimationPlan.<O>builder()
                .step(op)
                .build();
    }

    public static <O extends ISceneOps> AnimationPlan<O> instantAsync(
            Function<O, CompletableFuture<Void>> op
    ) {
        return AnimationPlan.<O>builder()
                .stepAsync(op)
                .build();
    }

    public static <O extends ISceneOps> AnimationPlan<O> empty() {
        return new AnimationPlan<>(Collections.emptyList());
    }

    public static final class Builder<O extends ISceneOps> {
        private final List<Step<O>> steps = new ArrayList<>();

        public Builder<O> step(int ticks) {
            return step(ticks, _ -> {});
        }

        public Builder<O> step(Consumer<O> op) {
            return step(1, op);
        }

        public Builder<O> step(int ticks, Consumer<O> op) {
            Objects.requireNonNull(op, "op");

            steps.add(new Step<>(ticks, scene -> {
                op.accept(scene);
                return CompletableFuture.completedFuture(null);
            }));

            return this;
        }

        public Builder<O> stepAsync(Function<O, CompletableFuture<Void>> op) {
            return stepAsync(1, op);
        }

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

        public AnimationPlan<O> build() {
            return new AnimationPlan<>(steps);
        }
    }
}