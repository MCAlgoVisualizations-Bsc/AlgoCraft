package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.ui.AudienceChannel;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public class DefaultScene extends AbstractScene {
    public DefaultScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] model) {
        for(int i = 0; i < model.length; i++) {
            var dv = model[i].getDisplayValue();
            this.addDisplay(i, dv);
            dv.setInstance(instance);
        }
    }

}

