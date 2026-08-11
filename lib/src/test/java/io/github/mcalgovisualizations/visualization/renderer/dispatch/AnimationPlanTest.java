package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.utils.TestSceneOps;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AnimationPlanTest {

    @Test
    void empty_returns_empty_plan() {
        var plan = AnimationPlan.<TestSceneOps>empty();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.steps().size());
    }

    @Test
    void instant_returns_single_step() {
        var executed = new AtomicBoolean(false);
        var plan = AnimationPlan.<TestSceneOps>instant(_ -> executed.set(true));

        assertEquals(1, plan.steps().size());
        var step = plan.steps().getFirst();
        assertEquals(1, step.ticks());

        step.run(null);
        assertTrue(executed.get());
    }

    @Test
    void instantAsync_returns_single_step() {
        var future = new CompletableFuture<Void>();
        var plan = AnimationPlan.<TestSceneOps>instantAsync(_ -> future);

        assertEquals(1, plan.steps().size());
        var step = plan.steps().getFirst();
        assertEquals(1, step.ticks());
        assertSame(future, step.run(null));
    }

    @Test
    void step_record_validations_and_getters() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnimationPlan.Step<TestSceneOps>(-1, _ -> CompletableFuture.completedFuture(null)));
        assertThrows(NullPointerException.class,
                () -> new AnimationPlan.Step<TestSceneOps>(1, null));

        var future = CompletableFuture.completedFuture((Void) null);
        var step = new AnimationPlan.Step<TestSceneOps>(2, _ -> future);

        assertEquals(2, step.ticks());
        assertNotNull(step.op());
        assertSame(future, step.run(null));
    }

    @Test
    void builder_step_returns_non_empty_plan() {
        var plan = AnimationPlan.<TestSceneOps>builder().step(0, _ -> {}).build();
        assertFalse(plan.isEmpty());
        assertEquals(1, plan.steps().size());
    }

    @Test
    void builder_step_with_negative_ticks_throws() {
        assertThrows(IllegalArgumentException.class, () -> AnimationPlan.<TestSceneOps>builder().step(-1, _ -> {}));
    }

    @Test
    void builder_step_overloads() {
        var executed = new AtomicBoolean(false);
        var plan = AnimationPlan.<TestSceneOps>builder()
                .step(5)
                .step(_ -> executed.set(true))
                .build();

        assertEquals(2, plan.steps().size());
        assertEquals(5, plan.steps().get(0).ticks());
        assertEquals(1, plan.steps().get(1).ticks());

        plan.steps().get(1).run(null);
        assertTrue(executed.get());
    }

    @Test
    void builder_step_null_op_throws() {
        assertThrows(NullPointerException.class, () -> AnimationPlan.<TestSceneOps>builder().step(1, null));
    }

    @Test
    void builder_stepAsync_overloads_and_null_future_handling() {
        var future = CompletableFuture.completedFuture((Void) null);
        var plan = AnimationPlan.<TestSceneOps>builder()
                .stepAsync(_ -> future)
                .stepAsync(3, _ -> null)
                .build();

        assertEquals(2, plan.steps().size());
        assertEquals(1, plan.steps().get(0).ticks());
        assertEquals(3, plan.steps().get(1).ticks());

        assertSame(future, plan.steps().get(0).run(null));
        assertNotNull(plan.steps().get(1).run(null), "Null returned from async operation should map to a completed future");
    }

    @Test
    void builder_stepAsync_null_op_throws() {
        assertThrows(NullPointerException.class, () -> AnimationPlan.<TestSceneOps>builder().stepAsync(1, null));
    }

    @Test
    void plan_steps_list_is_immutable() {
        var plan = AnimationPlan.<TestSceneOps>builder().step(1, _ -> {}).build();
        assertThrows(UnsupportedOperationException.class,
                () -> plan.steps().add(new AnimationPlan.Step<>(1, _ -> CompletableFuture.completedFuture(null))));
    }
}