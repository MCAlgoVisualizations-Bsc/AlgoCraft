package io.github.mcalgovisualizations.prefab.algorithms.sort.selection;

import io.github.mcalgovisualizations.prefab.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.prefab.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerSelectionSort implements IPlayerSort<SortingContext<Integer>> {
    @Override
    public void run(SortingContext<Integer> ctx) {
        final var values = ctx.getData();
        final var size = values.size();

        for (int i = 0; i < size; i++) {
            int minIndex = i;

            ctx.emit(new TrackI(i, values.get(i)));

            for (int j = i + 1; j < size; j++) {
                ctx.emit(new Compare(j, minIndex, values.get(j), values.get(minIndex)));

                if (values.get(j) < values.get(minIndex)) {
                    minIndex = j;
                    ctx.emit(new TrackMinIndex(minIndex, values.get(minIndex)));
                }
            }

            final var left = values.get(i);
            final var right = values.get(minIndex);

            ctx.swap(i, minIndex);
            ctx.emit(new Swap(i, minIndex, left, right));
        }
    }

    public record TrackI(int slot, Integer value) implements IAlgorithmEvent { }

    public record TrackMinIndex(int slot, Integer value) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        public AnimationPlan<SelectionScene> handle(TrackI event) {
            var plan = AnimationPlan.<SelectionScene>builder();

            plan.step(1, scene -> {
                scene.playSound("minecraft:block.note_block.hat", 0.6f, 1.0f);
                scene.trackI(event.slot());
            });

            return plan.build();
        }
    }

    public static class TrackMinIndexHandler implements IAnimationHandler<TrackMinIndex> {
        @Override
        public AnimationPlan<SelectionScene> handle(TrackMinIndex event) {
            var plan = AnimationPlan.<SelectionScene>builder();

            plan.step(3, scene -> {
                scene.playSound("minecraft:block.note_block.pling", 0.8f, 1.5f);

                final var message = Component.text("Found new min-value: ", NamedTextColor.GRAY)
                        .append(Component.text(event.value(), NamedTextColor.YELLOW, TextDecoration.BOLD));

                scene.sendActionBar(message);
            });

            plan.step(1, scene -> scene.trackMinIndex(event.slot()));

            return plan.build();
        }
    }

    public static class CompareHandler implements IAnimationHandler<Compare> {
        @Override
        public AnimationPlan<SelectionScene> handle(Compare event) {
            var plan = AnimationPlan.<SelectionScene>builder();

            plan.step(scene -> {
                        scene.resetLook();
                        scene.compare(event.x(), event.y());
                        scene.playSound("minecraft:block.note_block.xylophone", 0.6f, 1.2f);
                    })
                    .step(2, scene -> scene.trackJ(event.x()))
                    .step(1, scene -> {
                        final var message = Component.text("Comparing ", NamedTextColor.GRAY)
                                .append(Component.text("min", NamedTextColor.AQUA, TextDecoration.BOLD))
                                .append(Component.text(": ", NamedTextColor.GRAY))
                                .append(Component.text((int) event.yValue(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                                .append(Component.text(" vs ", NamedTextColor.GRAY))
                                .append(Component.text((int) event.xValue(), NamedTextColor.YELLOW, TextDecoration.BOLD));

                        scene.sendActionBar(message);
                    });

            return plan.build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        public AnimationPlan<SelectionScene> handle(Swap event) {
            var plan = AnimationPlan.<SelectionScene>builder();

            plan.step(1, scene -> {
                Component message;

                if (event.x() == event.y()) {
                    scene.playSound("minecraft:block.note_block.bell", 0.7f, 1.3f);

                    message = Component.text("✔ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                            .append(Component.text("Already in place: ", NamedTextColor.GRAY))
                            .append(Component.text((int) event.xValue(), NamedTextColor.GREEN, TextDecoration.BOLD));
                } else {
                    scene.playSound("minecraft:entity.item.pickup", 0.8f, 1.0f);

                    message = Component.text("⇄ ", NamedTextColor.GOLD, TextDecoration.BOLD)
                            .append(Component.text("Swap ", NamedTextColor.GRAY))
                            .append(Component.text((int) event.xValue(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                            .append(Component.text(" ↔ ", NamedTextColor.DARK_GRAY))
                            .append(Component.text((int) event.yValue(), NamedTextColor.YELLOW, TextDecoration.BOLD));
                }

                scene.sendActionBar(message);
            });

            plan.step(5, scene -> {
                scene.swap(event.x(), event.y());
                scene.markSorted(event.x());
            });

            return plan.build();
        }
    }
}