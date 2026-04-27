package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import org.jetbrains.annotations.NotNull;

public class GraphScene extends AbstractScene {
    public GraphScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int i = 0; i < layoutResults.length; i++) {
            if (layoutResults[i] != null) {
                addDisplay(i, layoutResults[i].displayValue());
            }
        }
    }
}
