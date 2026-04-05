package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class AnimationPlan {

    /**
     * @param ticks how long to wait AFTER running the op (can be 0)
     */
    public record Step(int ticks, Consumer<ISceneOps> op) {
            public Step(int ticks, Consumer<ISceneOps> op) {
                if (ticks < 1) throw new IllegalArgumentException("ticks must be >= 1");
                this.ticks = ticks;
                this.op = Objects.requireNonNull(op, "op");
            }
        }

    private final List<Step> steps;

    private AnimationPlan(List<Step> steps) {
        this.steps = List.copyOf(steps);
    }

    public List<Step> steps() {
        return steps;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AnimationPlan instant(Consumer<ISceneOps> op) {
        return builder().step(1, op).build();
    }

    public static AnimationPlan empty() {
        return new AnimationPlan(Collections.emptyList());
    }

    public static final class Builder {
        private final List<Step> steps = new ArrayList<>();

        public Builder step(int ticks) {
            return step(ticks, _ -> {});
        }

        public Builder step(Consumer<ISceneOps> op) {
            return step(1, op);
        }

        public Builder step(int ticks, Consumer<ISceneOps> op) {
            steps.add(new Step(ticks, op));
            return this;
        }

        public AnimationPlan build() {
            return new AnimationPlan(steps);
        }
    }
}
