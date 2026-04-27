package io.github.mcalgovisualizations.algorithms.sort.selection;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public class PlayerSelectionSort implements IPlayerSort<SortingContext<Integer>> {
    @Override
    public void run(SortingContext<Integer> ctx) {
        var size = ctx.getData().size();
        for (int i = 0; i < size; i++) {
            int minIndex = i;
            ctx.emit(new TrackI(i, ctx.getData().get(i)));
            for (int j = i + 1; j < size; j++) {
                if (ctx.getData().get(j) < ctx.getData().get(minIndex)) {
                    minIndex = j;
                }
            }
            ctx.swap(i, minIndex);
        }
    }

    public record TrackI(int i, Integer value) implements IAlgorithmEvent { }
    public record TrackJ(int j, Integer value) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(TrackI event) {
            var plan = AnimationPlan.builder();
            plan.step(1, scene -> {

            });
            return plan.build();
        }
    }

    public static class TrackJHandler implements IAnimationHandler<TrackJ> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(TrackJ event) {
            var plan = AnimationPlan.builder();
            plan.step(1, scene -> scene.setHighlighted(event.j(), true));
            return plan.build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<ISceneOps> handle(Swap event) {
            var plan = AnimationPlan.builder();
            plan.step(5, scene -> {
                scene.hoverDisplay(event.x(), true);
                scene.swapSlots(event.x(), event.y());
                scene.hoverDisplay(event.x(), false);
            });
            return plan.build();
        }
    }
}
