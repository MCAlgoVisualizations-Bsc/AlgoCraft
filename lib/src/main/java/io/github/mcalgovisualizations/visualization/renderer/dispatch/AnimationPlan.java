package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class AnimationPlan<O extends ISceneOps> {

    /**
     * @param ticks how long to wait AFTER running the op (can be 0)
     */
    public record Step<O extends ISceneOps>(int ticks, Consumer<O> op) {
            public Step(int ticks, Consumer<O> op) {
                if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
                this.ticks = ticks;
                this.op = Objects.requireNonNull(op, "op");
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

    public static <O extends ISceneOps>  Builder<O> builder() {
        return new Builder<>();
    }

    public static <O extends ISceneOps> AnimationPlan<O> instant(Consumer<O> op) {
        Builder<O> builder = builder();
        return builder.step(op).build();
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
            steps.add(new Step<>(ticks, op));
            return this;
        }

        public AnimationPlan<O> build() {
            return new AnimationPlan<>(steps);
        }
    }
}
