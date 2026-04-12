package io.github.mcalgovisualizations.visualization.layout;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public interface ILayout<I> {
    LayoutResult[] compute(I model, Pos origin, Instance instance);
}
