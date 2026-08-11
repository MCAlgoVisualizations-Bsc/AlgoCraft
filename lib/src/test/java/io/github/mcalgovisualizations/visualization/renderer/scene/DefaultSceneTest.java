package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.utils.TestDisplay;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSceneTest {

    private DefaultScene scene;
    private Pos origin;

    @BeforeEach
    void setUp() {
        origin = new Pos(1, 2, 3);
        var context = new SceneContext(null, null, origin);
        scene = new DefaultScene(context);
    }

    @Test
    void getOrigin_returns_origin_from_context() {
        assertEquals(origin, scene.getOrigin());
    }

    @Test
    void getDisplay_always_returns_null() {
        assertNull(scene.getDisplay(0));
        assertNull(scene.getDisplay(1));
    }

    @Test
    void setLayout_binds_displays_to_slots() {
        var coord1 = new Pos(0, 0, 0);
        var coord2 = new Pos(1, 1, 1);
        var display1 = new TestDisplay(coord1);
        var display2 = new TestDisplay(coord2);
        var model = new LayoutResult[]{
                new LayoutResult(null, new Pos(0, 0, 0), display1),
                new LayoutResult(null, new Pos(1, 1, 1), display2)
        };

        scene.setLayout(model);

        assertThrows(IllegalArgumentException.class, () -> scene.addDisplay(0, new TestDisplay(new Pos(0, 0, 0))));
        assertThrows(IllegalArgumentException.class, () -> scene.addDisplay(1, new TestDisplay(new Pos(0, 0, 0))));
    }

    @Test
    void setLayout_handles_empty_array() {
        assertDoesNotThrow(() -> scene.setLayout(new LayoutResult[0]));
    }
}