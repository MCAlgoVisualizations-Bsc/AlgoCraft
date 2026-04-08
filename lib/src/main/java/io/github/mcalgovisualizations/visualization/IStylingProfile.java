package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;

public interface IStylingProfile{
    IDisplayValue applyStyle(String value, Pos pos);
}
