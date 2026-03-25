package io.github.mcalgovisualizations.visualization.renderer.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;

public class CompleteHandler implements IAnimationHandler<Complete> {
    @Override
    public AnimationPlan handle(Complete event) {
        var plan = AnimationPlan.builder()
                .step(ISceneOps::stopAnimations)
                .step(sceneOps -> sceneOps.playEffect(2, "SUCCESS"));

        // hover each element with a small delay
        for (var i = 0; i < event.size(); i++) {
            final var idx = i;
            plan.step(1, sceneOps -> sceneOps.hoverDisplay(idx, true));
        }

        // hover each element with a small delay
        for (var i = 0; i < event.size(); i++) {
            final var idx = i;
            plan.step(1, sceneOps -> sceneOps.hoverDisplay(idx, false));
        }

        return plan.build();
    }
}
