package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.scenes.CaveTunnelScene;
import io.github.mcalgovisualizations.prefab.events.CaveVillagerMove;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
@Deprecated
public final class CaveVillagerMoveHandler implements IAnimationHandler<CaveVillagerMove> {
    @Override
    public <O extends ISceneOps> AnimationPlan<O> handle(CaveVillagerMove event) {
        return AnimationPlan.<O>builder()
                .step(8, sceneOps -> {
                    if (sceneOps instanceof CaveTunnelScene caveScene) {
                        caveScene.moveVillager(event.slot(), event.algorithmSpeed());
                        caveScene.updateLocatorBar(event.slot());
                    }
                })
                .build();
    }
}

