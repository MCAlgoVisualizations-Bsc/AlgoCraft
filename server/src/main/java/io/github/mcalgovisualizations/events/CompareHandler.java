package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class CompareHandler implements IAnimationHandler<Compare> {

    @Override
    public AnimationPlan handle(Compare event) {
        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.showHologram(Component.text(
                            "⚖ Comparing [" + event.xValue() + "] vs [" + event.yValue() + "]",
                            NamedTextColor.AQUA));
                    sceneOps.setHighlighted(event.x(), true);
                    sceneOps.setHighlighted(event.y(), true);
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
}


