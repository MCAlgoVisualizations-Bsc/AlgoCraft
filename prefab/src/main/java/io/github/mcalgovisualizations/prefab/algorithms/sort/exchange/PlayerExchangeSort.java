package io.github.mcalgovisualizations.prefab.algorithms.sort.exchange;

import io.github.mcalgovisualizations.prefab.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.prefab.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerExchangeSort implements IPlayerSort<SortingContext<Integer>> {
    @Override
    public void run(SortingContext<Integer> context) {
        final var values = context.getData();
        final var size = values.size();

        for (int i = 0; i < size; i++) {
            context.emit(new TrackI(i, values.get(i)));

            for (int j = i + 1; j < size; j++) {
                context.emit(new Compare(j, i, values.get(j), values.get(i)));

                if (values.get(i) > values.get(j)) {
                    final var left = values.get(i);
                    final var right = values.get(j);

                    context.swap(i, j);
                    context.emit(new Swap(i, j, left, right));
                }
            }

            context.emit(new MarkSorted(i, values.get(i)));
        }
    }

    public record TrackI(int slot, Integer value) implements IAlgorithmEvent { }

    public record MarkSorted(int slot, Integer value) implements IAlgorithmEvent { }

    public static class TrackIHandler implements IAnimationHandler<TrackI> {
        @Override
        public AnimationPlan<ExchangeScene> handle(TrackI event) {
            final var plan = AnimationPlan.<ExchangeScene>builder();

            plan.step(0, scene -> {
                scene.clearGlowing();
                scene.resetLookAt();
            });

            plan.stepAsync(1, scene ->
                    scene.stageCurrentMin(event.slot())
            );

            plan.step(1, scene -> {
                scene.highlightIndex(event.slot());
                scene.highlightCurrentMin();
            });

            plan.step(4, scene -> {
                scene.playSound("minecraft:block.note_block.hat", 0.6f, 1.0f);

                final var message = Component.text("▶ i=", NamedTextColor.AQUA, TextDecoration.BOLD)
                        .append(Component.text(event.slot(), NamedTextColor.AQUA, TextDecoration.BOLD))
                        .append(Component.text("  holding min ", NamedTextColor.GRAY))
                        .append(Component.text(event.value(), NamedTextColor.YELLOW, TextDecoration.BOLD));

                scene.sendActionBar(message);
            });

            return plan.build();
        }
    }

    public static class CompareHandler implements IAnimationHandler<Compare> {
        @Override
        public AnimationPlan<ExchangeScene> handle(Compare event) {
            final var plan = AnimationPlan.<ExchangeScene>builder();

            plan.step(1, scene -> {
                /*
                 * event.x() = j
                 * event.y() = i
                 */
                scene.clearGlowing();
                scene.setHighlighted(event.x(), true);

                scene.compareSlot(event.x());

                scene.highlightIndex(event.y());
                scene.highlightSlot(event.x());
                scene.highlightCurrentMin();
            });

            plan.step(4, scene -> {
                scene.playSound("minecraft:block.note_block.xylophone", 0.6f, 1.2f);

                final var message = Component.text((int) event.xValue(), NamedTextColor.YELLOW, TextDecoration.BOLD)
                        .append(Component.text(" < ", NamedTextColor.DARK_GRAY))
                        .append(Component.text((int) event.yValue(), NamedTextColor.AQUA, TextDecoration.BOLD))
                        .append(Component.text(" ?", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
                scene.sendActionBar(message);
            });

            return plan.build();
        }
    }

    public static class SwapHandler implements IAnimationHandler<Swap> {
        @Override
        public AnimationPlan<ExchangeScene> handle(Swap event) {
            final var plan = AnimationPlan.<ExchangeScene>builder();

            plan.stepAsync(1, scene ->
                scene.swapCurrentMinWithSlot(event.x(), event.y())
            );

            plan.step(1, scene -> {
                scene.clearGlowing();

                scene.highlightIndex(event.x());
                scene.highlightCurrentMin();
            });

            plan.step(4, scene -> {
                scene.playSound("minecraft:entity.item.pickup", 0.8f, 1.0f);

                final var message = Component.text("⇄ ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .append(Component.text("new min ", NamedTextColor.GRAY))
                        .append(Component.text((int) event.yValue(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .append(Component.text(" moves in", NamedTextColor.GRAY));

                scene.sendActionBar(message);
            });

            return plan.build();
        }
    }

    public static class MarkSortedHandler implements IAnimationHandler<MarkSorted> {
        @Override
        public AnimationPlan<ExchangeScene> handle(MarkSorted event) {
            final var plan = AnimationPlan.<ExchangeScene>builder();

            plan.step(0, AbstractScene::clearGlowing);

            plan.stepAsync(1, scene ->
                scene.markSorted(event.slot())
            );

            plan.step(4, scene -> {
                scene.playSound("minecraft:block.note_block.bell", 0.7f, 1.4f);

                final var message = Component.text("✔ ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text("sorted ", NamedTextColor.GRAY))
                        .append(Component.text(event.value(), NamedTextColor.GREEN, TextDecoration.BOLD));

                scene.sendActionBar(message);
            });

            return plan.build();
        }
    }
}