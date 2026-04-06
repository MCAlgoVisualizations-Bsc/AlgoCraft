package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.ISceneOps;

@FunctionalInterface
public interface SceneFactory<O extends ISceneOps> {
    O create(SceneContext ctx);
}
