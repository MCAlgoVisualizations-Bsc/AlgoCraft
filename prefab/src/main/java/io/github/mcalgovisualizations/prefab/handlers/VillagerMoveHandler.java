package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.algorithms.mazes.Scenes.GridScene;
import io.github.mcalgovisualizations.prefab.algorithms.mazes.Scenes.ILocatorBarScene;
import io.github.mcalgovisualizations.prefab.events.VillagerMove;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

public final class VillagerMoveHandler implements IAnimationHandler<VillagerMove> {
    @Override
    public <O extends ISceneOps> AnimationPlan<O> handle(VillagerMove event) {
        return AnimationPlan.<O>builder()
                .step(1, sceneOps -> {
                    // 1. Move the villager (GridScene now only takes 1 argument)
                    if (sceneOps instanceof GridScene gridScene) {
                        gridScene.moveVillager(event.slot());
                    }

                    // 2. Update the locator bar ONLY if the scene supports it
                    if (sceneOps instanceof ILocatorBarScene locatorScene) {
                        locatorScene.updateLocatorBar(event.slot());
                    }
                })
                .build();
    }
}