package io.github.mcalgovisualizations.scenes;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import org.jetbrains.annotations.NotNull;

public class InsertionTreeScene extends AbstractScene {
    private LayoutResult[] layoutResults;

    public InsertionTreeScene(@NotNull SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        this.layoutResults = layoutResults;
        displaysBySlot.put(0, layoutResults[0].displayValue());
    }

    public void insertNewLayout() {
        displaysBySlot.put(1, layoutResults[1].displayValue());
    }
}
