package io.github.mcalgovisualizations.prefab.layouts;

import io.github.mcalgovisualizations.visualization.layout.ILayout;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
@Deprecated
public record TSTNodeLayout<T extends Comparable<T>>() implements ILayout<List<T>> {
    @Override
    public LayoutResult[] compute(List<T> model, Pos origin, Instance instance) {
        return new LayoutResult[0];
    }
}
