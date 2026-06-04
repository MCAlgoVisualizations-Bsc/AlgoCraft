package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.events.Swap;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Converts swap events into highlight, hover, and slot swap animations.
 */
public final class SwapHandler implements IAnimationHandler<Swap> {
    /**
     * Builds the animation plan for a swap event.
     *
     * @param event swap event to render
     * @return animation plan for the swap action
     */
    @Override
    public AnimationPlan<ISceneOps> handle(Swap event) {
        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.sendActionBar(Component.text("Swapping [" + event.xValue() + "] and [" + event.yValue() + "]", NamedTextColor.GREEN));
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
                })
                .build();
    }
}


