package io.github.mcalgovisualizations.events;

import io.github.mcalgovisualizations.visualization.algorithms.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.algorithms.events.Swap;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SwapHandler implements IAnimationHandler<Swap> {
    @Override
    public AnimationPlan handle(Swap event) {
        return AnimationPlan.builder()
                .step(2, sceneOps -> {
                    sceneOps.showHologram(Component.text(
                            "↕ Swapping [" + event.xValue() + "] ↔ [" + event.yValue() + "]",
                            NamedTextColor.YELLOW));
                    sceneOps.setHighlighted(event.x(), true);
                    sceneOps.setHighlighted(event.y(), true);
                })
                .step(5, sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), true);
                    sceneOps.hoverDisplay(event.y(), true);
                })
                .step(5, sceneOps -> sceneOps.swapSlots(event.x(), event.y()))
                .step(5,  sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), false);
                    sceneOps.hoverDisplay(event.y(), false);
                })
                .step(5, sceneOps -> {
                    sceneOps.setHighlighted(event.x(), false);
                    sceneOps.setHighlighted(event.y(), false);
                    sceneOps.clearHologram();
                })
                .build();
    }
}


