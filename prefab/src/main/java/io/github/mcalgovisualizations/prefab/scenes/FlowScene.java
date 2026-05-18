package io.github.mcalgovisualizations.prefab.scenes;

import io.github.mcalgovisualizations.prefab.layouts.GraphNetworkLayout;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
@Deprecated
public class FlowScene extends DefaultScene {
    public FlowScene(SceneContext context) {
        super(context);
    }

    public void setValue(int slot, int value) {
        var display = (GraphNetworkLayout.NodeDisplay) requireDisplay(slot);
        display.setValue(value);
    }
}
