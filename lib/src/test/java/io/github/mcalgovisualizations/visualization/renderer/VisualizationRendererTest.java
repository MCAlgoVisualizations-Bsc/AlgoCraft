package io.github.mcalgovisualizations.visualization.renderer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VisualizationRendererTest {
    MinecraftServer server = MinecraftServer.init();
    private final Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
    private final Pos pos = Pos.ZERO;
    private Renderer renderer;

    @BeforeEach
    void beforeEach() {
        //this.renderer = new VisualizationRenderer(instance, pos, new FloatingLinearLayout());
    }

    @AfterEach
    void afterEach() {
        this.renderer = null;
    }

    @Test
    public void testRender() {
        assertEquals(true, true);
    }

}