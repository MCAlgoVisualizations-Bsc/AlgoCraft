package io.github.mcalgovisualizations.algorithms.sort.exchange;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public class PlayerExchangeSort implements IPlayerSort<SortingContext<Integer>> {
    @Override
    public void run(SortingContext<Integer> context) {
        final var values = context.getData();
        final var size = values.size();
        for (int i = 0; i < size; i++) {
            context.emit(new TrackI(i, values.get(i)));
            for (int j = i + 1; j < size; j++)
                if (values.get(i) > values.get(j)) {
                    final var temp = values.get(i);
                    values.set(i, values.get(j));
                    values.set(j, temp);
                }
        }
    }

    public record TrackI(int i, Integer value) implements IAlgorithmEvent { }

    public record TrackJ(int j, Integer value) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(TrackI event) {
            final var plan = AnimationPlan.builder();
            return plan.build();
        }
    }

    public static class TrackJHandler implements IAnimationHandler<TrackJ> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(TrackJ event) {
            final var plan = AnimationPlan.builder();
            return plan.build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(Swap event) {
            final var plan = AnimationPlan.builder();
            return plan.build();
        }
    }

    public static class CompareHandler implements IAnimationHandler<Compare> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(Compare event) {
            final var plan = AnimationPlan.builder();
            return plan.build();
        }
    }
}
