package io.github.mcalgovisualizations.layouts;

import io.github.mcalgovisualizations.Displays.NodeDisplay;
import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.ui.IStylingProfile;
import net.minestom.server.coordinate.Pos;

public class NodeProfile  implements IStylingProfile {
    @Override
    public IDisplayValue applyStyle(String value, Pos pos) {
        System.out.println(value);
        System.out.println(pos);
        return new NodeDisplay(value, pos);
    }
}
