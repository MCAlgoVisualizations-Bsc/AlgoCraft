package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import org.jetbrains.annotations.Nullable;

/**
 * Default scene implementation that binds one display per slot.
 */
public class DefaultScene extends AbstractScene {
    /**
     * Creates the default scene.
     *
     * @param context the scene context
     */
    public DefaultScene(SceneContext context) {
        super(context);
    }

    @Override
    /**
     * Spawns all layout results into sequential slots.
     *
     * @param model layout results from the renderer
     */
    public void setLayout(LayoutResult[] model) {
        for(int i = 0; i < model.length; i++) {
            var dv = model[i].displayValue();
            this.addDisplay(i, dv);
            dv.setInstance(instance);
        }
    }

    @Override
    /**
     * Returns the display at the given slot.
     *
     * @param slot slot index
     * @return the display at that slot, or null in the current default implementation
     */
    public @Nullable IDisplayValue getDisplay(int slot) {
        return null;
    }
}

