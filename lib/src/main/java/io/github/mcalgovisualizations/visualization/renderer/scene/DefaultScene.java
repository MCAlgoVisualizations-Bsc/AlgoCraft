package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;

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
}

