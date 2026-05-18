package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.algorithms.GraphSearch.GraphScene;
import io.github.mcalgovisualizations.prefab.events.PathFound;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.stream.Collectors;

public class PathFoundHandler implements IAnimationHandler<PathFound> {

    @Override
    public AnimationPlan<GraphScene> handle(PathFound event) {
        return AnimationPlan.<GraphScene>builder()
                .step(sceneOps -> {
                    String pathString = event.path().stream()
                            .map(node -> String.valueOf(node.getValue()))
                            .collect(Collectors.joining(" -> "));

                    sceneOps.sendMessage(Component.text("Path found: " + pathString, NamedTextColor.GREEN));
                })
                .build();
    }
}
