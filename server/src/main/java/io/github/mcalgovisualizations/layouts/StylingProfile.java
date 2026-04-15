package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.visualization.ui.IStylingProfile;
import io.github.mcalgovisualizations.Displays.MobDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;

public class StylingProfile implements IStylingProfile {
    @Override
    public IDisplayValue applyStyle(String value, Pos pos) {
        return new MobDisplay(pos, value);
    }
}
