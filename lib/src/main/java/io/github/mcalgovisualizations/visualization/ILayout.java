package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;

public interface ILayout {
    <T extends Comparable<T>> LayoutResult<T>[] compute(List<Data<T>> model, Pos origin, Instance instance);
}
