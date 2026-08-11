package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimationPlanTest {
    @Test
    void empty_returns_empty_plan() {
        var plan = AnimationPlan.empty();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.steps().size());
    }

    @Test
    void instant_returns_single_step() {
        var plan = AnimationPlan.instant(_ -> {});
        assertEquals(1, plan.steps().size());
        assertEquals(1, plan.steps().getFirst().ticks());
    }

    @Test
    void builder_step_returns_non_empty_plan() {
        var plan = AnimationPlan.builder().step(0, _ -> {}).build();
        assertFalse(plan.isEmpty());
        assertEquals(1, plan.steps().size());
    }

    @Test
    void builder_step_with_negative_ticks_throws() {
        assertThrows(IllegalArgumentException.class, () -> AnimationPlan.builder().step(-1, _ -> {}));
    }
}