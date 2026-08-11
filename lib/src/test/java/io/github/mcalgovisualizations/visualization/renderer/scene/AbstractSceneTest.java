package io.github.mcalgovisualizations.visualization.renderer.scene;

import io.github.mcalgovisualizations.visualization.renderer.IDisplayValue;
import io.github.mcalgovisualizations.visualization.utils.TestDisplay;
import io.github.mcalgovisualizations.visualization.utils.TestSceneOps;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSceneTest {

    private interface SubDisplayValue extends IDisplayValue {}

    private static class SubTestDisplay extends TestDisplay implements SubDisplayValue  {
        public SubTestDisplay(Pos pos) {
            super(pos);
        }
    }

    private TestSceneOps scene;
    private Pos origin;

    @BeforeEach
    void setUp() {
        origin = new Pos(10, 20, 30);
        var context = new SceneContext(null, null, origin);
        scene = new TestSceneOps(context);
    }

    @Test
    void getOrigin_returns_origin_from_context() {
        assertEquals(origin, scene.getOrigin());
    }

    @Test
    void addDisplay_stores_display_and_sets_instance() {
        var display = new TestDisplay(new Pos(0, 0, 0));
        scene.addDisplay(0, display);

        assertSame(display, scene.getDisplay(0));
    }

    @Test
    void addDisplay_throws_illegal_argument_exception_when_slot_occupied() {
        var display = new TestDisplay(new Pos(0, 0, 0));
        scene.addDisplay(0, display);

        assertThrows(IllegalArgumentException.class, () -> scene.addDisplay(0, new TestDisplay(new Pos(1, 1, 1))));
    }

    @Test
    void getDisplay_returns_null_when_slot_empty() {
        assertNull(scene.getDisplay(99));
    }

    @Test
    void setHighlighted_updates_glowing_state_and_tracks_slot() {
        var display = new TestDisplay(new Pos(0, 0, 0));
        scene.addDisplay(1, display);

        scene.setHighlighted(1, true);
        assertTrue(display.isGlowing());

        scene.setHighlighted(1, false);
        assertFalse(display.isGlowing());
    }

    @Test
    void setHighlighted_throws_illegal_state_exception_when_slot_missing() {
        assertThrows(IllegalStateException.class, () -> scene.setHighlighted(5, true));
    }

    @Test
    void clearGlowing_resets_glowing_and_clears_highlighted_slots() {
        var display1 = new TestDisplay(new Pos(0, 0, 0));
        var display2 = new TestDisplay(new Pos(0, 0, 0));

        scene.addDisplay(1, display1);
        scene.addDisplay(2, display2);

        scene.setHighlighted(1, true);
        scene.setHighlighted(2, true);

        scene.clearGlowing();

        assertFalse(display1.isGlowing());
        assertFalse(display2.isGlowing());
    }

    @Test
    void hoverDisplay_moves_display_up_and_down() {
        var initialPos = new Pos(0, 10, 0);
        var display = new TestDisplay(initialPos);
        scene.addDisplay(0, display);

        scene.hoverDisplay(0, true);
        assertEquals(new Pos(0, 11, 0), display.getPos());

        scene.hoverDisplay(0, false);
        assertEquals(new Pos(0, 10, 0), display.getPos());
    }

    @Test
    void moveSlotTo_teleports_display_to_target_position() {
        var display = new TestDisplay(new Pos(0, 0, 0));
        var targetPos = new Pos(5, 5, 5);
        scene.addDisplay(0, display);

        scene.moveSlotTo(0, targetPos);
        assertEquals(targetPos, display.getPos());
    }

    @Test
    void walkSlotTo_executes_walkTo_and_returns_future() {
        var display = new TestDisplay(new Pos(0, 0, 0));
        var targetPos = new Pos(3, 3, 3);
        scene.addDisplay(0, display);

        var future = scene.walkSlotTo(0, targetPos);

        assertNotNull(future);
        assertTrue(future.isDone());
        assertEquals(targetPos, display.getPos());
    }

    @Test
    void swapSlots_swaps_map_references_and_positions() {
        var posA = new Pos(1, 0, 0);
        var posB = new Pos(2, 0, 0);

        var displayA = new TestDisplay(posA);
        var displayB = new TestDisplay(posB);

        scene.addDisplay(0, displayA);
        scene.addDisplay(1, displayB);

        scene.swapSlots(0, 1);

        assertSame(displayB, scene.getDisplay(0));
        assertSame(displayA, scene.getDisplay(1));
        assertEquals(posB, displayA.getPos());
        assertEquals(posA, displayB.getPos());
    }

    @Test
    void getDisplaySlots_filters_by_subclass() {
        var baseDisplay = new TestDisplay(new Pos(0, 0, 0));
        var subDisplay = new SubTestDisplay(new Pos(0, 0, 0));

        scene.addDisplay(0, baseDisplay);
        scene.addDisplay(1, subDisplay);

        var filtered = scene.getDisplaySlots(SubDisplayValue.class);

        assertEquals(1, filtered.size());
        assertSame(subDisplay, filtered.get(1));
    }

    @Test
    void cleanUp_removes_all_displays_and_clears_state() {
        var display1 = new TestDisplay(new Pos(0, 0, 0));
        var display2 = new TestDisplay(new Pos(0, 0, 0));

        scene.addDisplay(0, display1);
        scene.addDisplay(1, display2);
        scene.setHighlighted(0, true);

        scene.cleanUp();

        assertNull(scene.getDisplay(0));
        assertNull(scene.getDisplay(1));
    }
}