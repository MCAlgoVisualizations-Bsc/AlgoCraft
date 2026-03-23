package io.github.mcalgovisualizations.visualization.renderer.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.Validate;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ValidateHandler implements IAnimationHandler<Validate> {
    @Override
    public AnimationPlan handle(Validate event) {
        return AnimationPlan.builder()
                .step(2, sceneOps -> sceneOps.showHologram(Component.text(
                        "✔ Already in place!", NamedTextColor.GREEN)))
                .step(15, ISceneOps::clearHologram)
                .build();
    }
}
