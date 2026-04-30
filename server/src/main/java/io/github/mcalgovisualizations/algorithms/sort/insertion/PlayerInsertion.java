package io.github.mcalgovisualizations.algorithms.sort.insertion;

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

        for (int i = 1; i < values.size(); i++) {
            int j = i;

            ctx.emit(new TrackI(i, values.get(i)));

            while (j > 0) {
                var current = values.get(j);
                var previous = values.get(j - 1);

                ctx.emit(
                        new TrackJ(j, current),
                        new Compare(j, j - 1, previous, current)
                );

                if (current.compareTo(previous) >= 0) {
                    break;
                }

                ctx.emit(new Swap(j, j - 1, previous, current));
                ctx.swap(j, j - 1);

                j--;
            }

            ctx.emit(new Inserted(j, values.get(j)));
        }
    }

    public record TrackI(int idx, Object value) implements IAlgorithmEvent { }

    public record TrackJ(int idx, Object value) implements IAlgorithmEvent { }

    public record Inserted(int idx, Object value) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        public AnimationPlan<InsertionScene> handle(TrackI event) {
            return AnimationPlan.<InsertionScene>builder()
                    .step(scene -> {
                        scene.finishInnerLoopVisuals();
                        scene.clearGlowing();
                        scene.revealInitialPrefixIfNeeded();
                    })
                    .step(4, scene -> {
                        scene.revealInitialPrefixIfNeeded();
                        scene.sendActionBar(Component.text(
                                "Sorted prefix starts here",
                                NamedTextColor.GRAY
                        ));
                    })
                    .step(4, scene -> {
                        scene.revealSlot(event.idx());
                        scene.sendActionBar(Component.text(
                                "Insert [" + event.value() + "] into sorted prefix",
                                NamedTextColor.AQUA
                        ));
                    })
                    .build();
        }
    }

    public static class TrackJHandler implements IAnimationHandler<TrackJ> {
        @Override
        public AnimationPlan<InsertionScene> handle(TrackJ event) {
            return AnimationPlan.<InsertionScene>builder()
                    .step(4, scene -> scene.sendActionBar(Component.text(
                            "Scan left → [" + event.value() + "]",
                            NamedTextColor.GREEN
                    )))
                    .step(scene -> {
                        scene.markJ(event.idx());
                        scene.setHighlighted(event.idx(), true);
                    })
                    .build();
        }
    }

    public static class CompareHandler implements IAnimationHandler<Compare> {
        @Override
        public AnimationPlan<InsertionScene> handle(Compare event) {
            return AnimationPlan.<InsertionScene>builder()
                    .step(4, scene -> {
                        scene.playSound("block.note_block.hat", 0.6f, 1.8f);
                        scene.sendActionBar(Component.text(
                                "Is " + event.xValue() + " > " + event.yValue() + " ?",
                                NamedTextColor.YELLOW
                        ));
                    })
                    .step(scene -> scene.comparePulse(event.x(), event.y()))
                    .build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        public AnimationPlan<InsertionScene> handle(Swap event) {
            return AnimationPlan.<InsertionScene>builder()
                    .step(4, scene -> {
                        scene.playSound("block.piston.extend", 0.7f, 1.2f);
                        scene.sendActionBar(Component.text(
                                "Shift [" + event.xValue() + "] right",
                                NamedTextColor.GOLD
                        ));
                    })
                    .step(scene -> scene.danceSwap(event.x(), event.y()))
                    .step(2, scene -> scene.commitSwap(event.x(), event.y()))
                    .build();
        }
    }

    public static class InsertedHandler implements IAnimationHandler<Inserted> {
        @Override
        public AnimationPlan<InsertionScene> handle(Inserted event) {
            return AnimationPlan.<InsertionScene>builder()
                    .step(4, scene -> {
                        scene.markInserted(event.idx());
                        scene.sendActionBar(Component.text(
                                "Inserted [" + event.value() + "]",
                                NamedTextColor.GRAY
                        ));
                    })
                    .step(scene -> scene.playSound("block.note_block.pling", 0.7f, 1.6f))
                    .build();
        }
    }
}