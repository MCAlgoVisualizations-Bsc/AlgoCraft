package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class CompareHandler implements IAnimationHandler<Compare> {

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


