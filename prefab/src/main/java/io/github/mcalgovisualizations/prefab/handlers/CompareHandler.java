package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Converts compare events into highlight and hover animations.
 */
public final class CompareHandler implements IAnimationHandler<Compare> {

    /**
     * Builds the animation plan for a compare event.
     *
     * @param event compare event to render
     * @return animation plan for the compare action
     */
    @Override
    public AnimationPlan<ISceneOps> handle(Compare event) {
        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.sendActionBar(Component.text("Comparing [" + event.xValue() + "] and [" + event.yValue() + "]", NamedTextColor.GREEN));
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
                })
                .build();
    }
}


