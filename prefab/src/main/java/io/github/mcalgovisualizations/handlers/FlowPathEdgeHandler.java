package io.github.mcalgovisualizations.handlers;
import io.github.mcalgovisualizations.events.FlowPathEdge;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
@Deprecated
public final class FlowPathEdgeHandler implements IAnimationHandler<FlowPathEdge> {
    @Override
    public AnimationPlan<ISceneOps> handle(FlowPathEdge event) {
        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    sceneOps.sendActionBar(Component.text(
                            "Chosen path " + event.from() + " -> " + event.to() + " (flow " + event.pathFlow() + ")",
                            NamedTextColor.RED
                    ));
                    sceneOps.setHighlighted(event.slot(), true);
                })
                .step(1, sceneOps -> sceneOps.hoverDisplay(event.slot(), true))
                .step(1, sceneOps -> sceneOps.hoverDisplay(event.slot(), false))
                .step(1, sceneOps -> sceneOps.setHighlighted(event.slot(), false))
                .build();
    }
}
