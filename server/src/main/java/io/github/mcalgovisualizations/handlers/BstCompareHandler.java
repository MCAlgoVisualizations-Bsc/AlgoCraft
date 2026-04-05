package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class BstCompareHandler implements IAnimationHandler<Compare> {

    public enum Mode {
        BUILD,
        SEARCH
    }

    private final Mode mode;

    public BstCompareHandler(Mode mode) {
        this.mode = mode;
    }

    @Override
    public AnimationPlan handle(Compare event) {
        String narration = buildNarration(event);

        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.showHologram(Component.text(
                            "BST compare [" + event.xValue() + "] vs [" + event.yValue() + "]",
                            NamedTextColor.AQUA));
                    sceneOps.setHighlighted(event.x(), true);
                    sceneOps.setHighlighted(event.y(), true);
                    sceneOps.sendMessage(Component.text(narration, NamedTextColor.GRAY));
                })
                .step(1, sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), true);
                    sceneOps.hoverDisplay(event.y(), true);
                })
                .step(1, sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), false);
                    sceneOps.hoverDisplay(event.y(), false);
                })
                .step(1, sceneOps -> {
                    sceneOps.setHighlighted(event.x(), false);
                    sceneOps.setHighlighted(event.y(), false);
                    sceneOps.clearHologram();
                })
                .build();
    }

    private String buildNarration(Compare event) {
        if (!(event.xValue() instanceof Integer current) || !(event.yValue() instanceof Integer candidate)) {
            return "BST step";
        }

        if (candidate < current) {
            return mode == Mode.SEARCH
                    ? "Go left from " + current
                    : "Try inserting " + candidate + " to the left of " + current;
        }
        if (candidate > current) {
            return mode == Mode.SEARCH
                    ? "Go right from " + current
                    : "Try inserting " + candidate + " to the right of " + current;
        }

        return mode == Mode.SEARCH
                ? "Found " + candidate
                : "Value equals current node " + current;
    }
}

