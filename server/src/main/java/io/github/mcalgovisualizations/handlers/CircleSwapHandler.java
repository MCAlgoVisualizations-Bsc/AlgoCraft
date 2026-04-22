package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.CircleScene;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CircleSwapHandler implements IAnimationHandler<Swap> {

    @Override
    public AnimationPlan<CircleScene> handle(Swap event) {
        return AnimationPlan.<CircleScene>builder()
                .step(scene -> scene.sendActionBar(Component.text(
                        event.xValue() + " is larger than " + event.yValue(),
                        NamedTextColor.YELLOW)))
                .step(2, scene -> {
                    int left = event.x();
                    int right = event.y();

                    if (scene.hasStagedCompare()) {
                        if (scene.isStagedPair(left, right)) {
                            scene.swapStagedComparePositions();
                            return;
                        }

                        scene.restoreStagedCompare();
                        scene.swapDirect(left, right);
                        return;
                    }

                    scene.swapDirect(left, right);
                })
                .step(2, scene -> {
                    int left = event.x();
                    int right = event.y();

                    if (scene.hasStagedCompare() && scene.isStagedPair(left, right)) {
                        scene.commitStagedSwap();
                    }
                })
                .build();
    }
}