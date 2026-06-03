package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.instance.AudienceChannel;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

/**
 * Immutable runtime context used to construct a scene.
 *
 * @param instance the Minestom instance that backs the visualization
 * @param audience the audience channel used for feedback
 * @param origin the spatial anchor for layout placement
 */
public record SceneContext(Instance instance, AudienceChannel audience, Pos origin) { }
