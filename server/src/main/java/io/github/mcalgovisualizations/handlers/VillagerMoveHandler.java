package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.GridScene;
import io.github.mcalgovisualizations.events.VillagerMove;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class VillagerMoveHandler implements IAnimationHandler<VillagerMove> {
    @Override
    public <O extends ISceneOps> AnimationPlan<O> handle(VillagerMove event) {
        return AnimationPlan.<O>builder()
                .step(4, sceneOps -> {
                    if (sceneOps instanceof GridScene gridScene) {
                        gridScene.moveVillager(event.slot(), event.algorithmSpeed());
                        gridScene.updateLocatorBar(event.slot());
                    }
                })
                .build();
    }
}

