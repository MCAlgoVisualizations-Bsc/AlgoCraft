package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.CircleScene;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CircleCompareHandler implements IAnimationHandler<Compare> {

    @Override
    public AnimationPlan<CircleScene> handle(Compare event) {
        return AnimationPlan.<CircleScene>builder()
                .step(scene -> scene.sendMessage(Component.text(
                        "Comparing [" + event.xValue() + "] with [" + event.yValue() + "]",
                        NamedTextColor.YELLOW)))
                .step(5, scene -> {
                    int currentLeft = event.x();
                    int currentRight = event.y();

                    if (scene.hasStagedCompare()) {
                        if (scene.isStagedPair(currentLeft, currentRight)) {
                            return;
                        }
                        scene.restoreStagedCompare();
                    }

                    scene.stageCompare(currentLeft, currentRight);
                    scene.trackDisplay(scene.getStagedRightDisplay());
                    scene.sendActionBar(Component.text("currently tracking : " + event.xValue()));
                })
                .build();
    }
}