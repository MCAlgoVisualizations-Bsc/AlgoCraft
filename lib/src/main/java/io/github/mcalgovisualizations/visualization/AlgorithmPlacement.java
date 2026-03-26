package io.github.mcalgovisualizations.visualization;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record AlgorithmPlacement(@NotNull Pos renderOrigin, @NotNull Pos teleportPoint) { }
