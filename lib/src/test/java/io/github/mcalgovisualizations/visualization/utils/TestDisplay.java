package io.github.mcalgovisualizations.visualization.utils;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TestDisplay implements IDisplayValue {
    private boolean glowing = false;
    private Pos pos;
    private Instance instance;
    private boolean removed;

    public TestDisplay(Pos pos) {
        this.pos = pos;
    }

    @Override
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isGlowing() {
        return this.glowing;
    }

    @Override
    public boolean isSpawned() {
        return false;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    @Override
    public void teleport(@NotNull Pos pos) {
        this.pos = pos;
    }

    @Override
    public CompletableFuture<Void> walkTo(@NotNull Pos pos) {
        this.pos = pos;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void setInstance(Instance instance, Pos pos) { }

    @Override
    public void addViewer(Player player) { }

    @Override
    public void remove() {
        this.removed = true;
    }
}
