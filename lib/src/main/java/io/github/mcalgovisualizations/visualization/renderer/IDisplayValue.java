package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public interface IDisplayValue {
    Pos getPos();
    void setValue(int value);
    void setInstance();
    void addViewer(Player player);
    void remove();
    void teleport(Pos pos);
    void setGlowing(boolean highlighted);
    boolean isSpawned();
}
