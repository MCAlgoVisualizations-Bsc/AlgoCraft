package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import org.jetbrains.annotations.Nullable;

public class DefaultScene extends AbstractScene {
    public DefaultScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] model) {
        for(int i = 0; i < model.length; i++) {
            var dv = model[i].displayValue();
            this.addDisplay(i, dv);
            dv.setInstance(instance);
        }
    }

    @Override
    public @Nullable IDisplayValue getDisplay(int slot) {
        return null;
    }
}

