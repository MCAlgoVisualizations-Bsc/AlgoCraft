package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.events.FlowEdgeVisit;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
@Deprecated
public final class FlowEdgeVisitHandler implements IAnimationHandler<FlowEdgeVisit> {

    @Override
    public AnimationPlan<ISceneOps> handle(FlowEdgeVisit event) {
        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.sendActionBar(Component.text(
                            "Explore edge " + event.from() + " -> " + event.to() +
                                    " (residual " + event.residualCapacity() + ")",
                            NamedTextColor.YELLOW
                    ));
                    sceneOps.hoverDisplay(event.slot(), true);
                })
                .step(1, sceneOps -> sceneOps.hoverDisplay(event.slot(), false))
                .build();
    }
}
