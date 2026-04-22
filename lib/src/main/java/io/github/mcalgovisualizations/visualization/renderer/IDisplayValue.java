package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public interface IDisplayValue {
    Pos getPos();
    void setInstance(Instance instance);
    void addViewer(Player player);
    void remove();
    void teleport(Pos pos);
    void setGlowing(boolean highlighted);
    boolean isSpawned();
}
