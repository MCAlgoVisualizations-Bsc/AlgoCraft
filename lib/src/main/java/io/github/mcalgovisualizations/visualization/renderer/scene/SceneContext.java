package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.instance.AudienceChannel;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

public record SceneContext(Instance instance, AudienceChannel audience, Pos origin) {
}
