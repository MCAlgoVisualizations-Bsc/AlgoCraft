package io.github.mcalgovisualizations.visualization.layouts;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;


public interface Layout<T extends Comparable<T>, M> {
    LayoutResult<T>[] compute(M model, Pos origin, Instance instance);
}
