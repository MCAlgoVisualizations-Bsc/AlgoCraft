package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.events.FlowStatus;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
@Deprecated
public final class FlowStatusHandler implements IAnimationHandler<FlowStatus> {

    @Override
    public AnimationPlan<ISceneOps> handle(FlowStatus event) {
        return switch (event.type()) {
            case INVALID_INPUT -> AnimationPlan.instant(scene -> scene.sendMessage(
                    Component.text("Max-Flow: invalid matrix input", NamedTextColor.RED)
            ));
            case AUGMENTED -> AnimationPlan.instant(scene -> {
                Component action = Component.text(
                        "Augment +" + event.value() + " | total " + event.total(),
                        NamedTextColor.AQUA
                );
                scene.sendActionBar(action);
                scene.sendMessage(action.color(NamedTextColor.GRAY));
            });
            case COMPLETE -> AnimationPlan.instant(scene -> {
                Component message = Component.text(
                        "Max-Flow complete: " + event.total(),
                        NamedTextColor.GREEN
                );
                scene.sendActionBar(message);
                scene.sendMessage(message);
            });
        };
    }
}

