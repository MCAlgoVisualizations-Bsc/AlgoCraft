package io.github.mcalgovisualizations.pov;

import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SceneAdapterRegistry {
    private final Map<Class<? extends ISceneOps>, VillagerPovSceneAdapter<?>> adapters = new LinkedHashMap<>();

    public <S extends ISceneOps> SceneAdapterRegistry register(Class<S> sceneType, VillagerPovSceneAdapter<? super S> adapter) {
        if (sceneType == null || adapter == null) {
            throw new IllegalArgumentException("sceneType and adapter must be non-null");
        }
        adapters.put(sceneType, adapter);
        return this;
    }

    public boolean supports(ISceneOps scene) {
        return resolve(scene).isPresent();
    }

    @SuppressWarnings("unchecked")
    public <S extends ISceneOps> Optional<VillagerPovSceneAdapter<S>> resolve(S scene) {
        if (scene == null) {
            return Optional.empty();
        }

        for (var entry : adapters.entrySet()) {
            if (entry.getKey().isInstance(scene)) {
                return Optional.of((VillagerPovSceneAdapter<S>) entry.getValue());
            }
        }

        return Optional.empty();
    }
}

