package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.renderer.Displays.MobDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;

public class StylingProfile implements IStylingProfile {
    @Override
    public IDisplayValue applyStyle(String value, Pos pos) {
        return new MobDisplay(pos, value);
    }
}
