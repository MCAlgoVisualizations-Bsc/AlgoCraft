package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.CircleScene;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerInsertion<T extends Comparable<T>> implements IPlayerSort<SortingContext<T>> {

    @Override
    public void run(SortingContext<T> ctx) {
        var values = ctx.getData();
        int n = values.size();

        for (int i = 1; i < n; i++) {
            int j = i;

            ctx.emit(new TrackI(i));

            while (j > 0) {
                var x = values.get(j);
                var y = values.get(j - 1);

                ctx.emit(new TrackJ(j), new Compare(j, j - 1, y, x));

                if (x.compareTo(y) >= 0) {
                    break;
                }

                ctx.emit(new Swap(j, j - 1, y, x));
                ctx.swap(j, j - 1);
                j--;
            }

            ctx.emit(new TrackJ(j));
        }
    }

    public record TrackI(int idx) implements IAlgorithmEvent { }
    public record TrackJ(int idx) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        public AnimationPlan<CircleScene> handle(TrackI event) {
            return AnimationPlan.<CircleScene>builder()
                    .step(scene -> {
                        scene.trackI(event.idx());
                        scene.sendActionBar(Component.text(
                                "i : [" + event.idx + "]",
                                NamedTextColor.AQUA
                        ));
                    })
                    .build();
        }
    }

    public static class TrackJHandler implements IAnimationHandler<TrackJ> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<CircleScene> handle(TrackJ event) {
            return AnimationPlan.<CircleScene>builder()
                    .step(scene -> {
                        scene.trackJ(event.idx());
                        scene.sendActionBar(Component.text(
                                "j : [" + event.idx + "]",
                                NamedTextColor.GREEN
                        ));
                    })
                    .build();
        }
    }

    public static class CompareHandler implements IAnimationHandler<Compare> {
        @Override
        @SuppressWarnings("unchecked")
        public AnimationPlan<CircleScene> handle(Compare event) {
            return AnimationPlan.<CircleScene>builder()
                    .step(scene -> scene.sendMessage(Component.text(
                            "Comparing [" + event.xValue() + "] with [" + event.yValue() + "]",
                            NamedTextColor.YELLOW)))
                    .step(scene -> {
                        int left = event.x();
                        int right = event.y();
                        if (scene.hasStagedCompare()) {
                            if (scene.isStagedPair(left, right)) {
                                return;
                            }
                            scene.restoreStagedCompare();
                        }
                        scene.stageCompare(left, right);
                    })
                    .build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        public AnimationPlan<CircleScene> handle(Swap event) {
            return AnimationPlan.<CircleScene>builder()
                    .step(scene -> scene.sendActionBar(Component.text(
                            event.xValue() + " is larger than " + event.yValue(),
                            NamedTextColor.YELLOW)))
                    .step(scene -> {
                        int left = event.x();
                        int right = event.y();

                        if (scene.hasStagedCompare()) {
                            if (scene.isStagedPair(left, right)) {
                                scene.swapStagedComparePositions();
                                return;
                            }

                            scene.restoreStagedCompare();
                            scene.swapDirect(left, right);
                            return;
                        }

                        scene.swapDirect(left, right);
                    })
                    .step(2, scene -> {
                        int left = event.x();
                        int right = event.y();

                        if (scene.hasStagedCompare() && scene.isStagedPair(left, right)) {
                            scene.commitStagedSwap();
                        }
                    })
                    .build();
        }
    }
}