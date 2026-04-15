package io.github.mcalgovisualizations.handlers;
import io.github.mcalgovisualizations.events.VillagerMove;
import io.github.mcalgovisualizations.GridScene;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
public final class VillagerMoveHandler implements IAnimationHandler<VillagerMove> {
    @Override
    public AnimationPlan<GridScene> handle(VillagerMove event) {
        return AnimationPlan.instant(sceneOps ->
                sceneOps.moveVillager(event.slot())
        );
    }
}
