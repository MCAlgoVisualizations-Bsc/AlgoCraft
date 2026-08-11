package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.TaskScheduler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.utils.TestSceneOps;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExecutorTest {

    private TestSceneOps scene;
    private TaskScheduler scheduler;
    private TaskScheduler.TaskHandle taskHandle;
    private Executor<TestSceneOps> executor;

    @BeforeEach
    void setUp() {
        var context = new SceneContext(null, null, new Pos(0, 0, 0));
        scene = new TestSceneOps(context);
        scheduler = mock(TaskScheduler.class);
        taskHandle = mock(TaskScheduler.TaskHandle.class);

        when(scheduler.schedule(any(), any())).thenReturn(taskHandle);

        executor = new Executor<>(scene, scheduler);
    }

    @Test
    void constructor_throws_null_pointer_exception_for_null_scene() {
        assertThrows(NullPointerException.class, () -> new Executor<>(null, scheduler));
    }

    @Test
    void constructor_throws_null_pointer_exception_for_null_scheduler() {
        assertThrows(NullPointerException.class, () -> new Executor<>(scene, null));
    }

    @Test
    void initial_state_is_idle() {
        assertTrue(executor.isIdle());
    }

    @Test
    void add_ignores_empty_plan() {
        executor.add(AnimationPlan.empty());
        assertTrue(executor.isIdle());
    }

    @Test
    void add_enqueues_non_empty_plan() {
        var plan = AnimationPlan.<TestSceneOps>builder().step(0, ops -> CompletableFuture.completedFuture(null)).build();
        executor.add(plan);
        assertFalse(executor.isIdle());
    }

    @Test
    void setSpeed_throws_illegal_argument_exception_for_invalid_speed() {
        assertThrows(IllegalArgumentException.class, () -> executor.setSpeed(0));
        assertThrows(IllegalArgumentException.class, () -> executor.setSpeed(-1));
    }

    @Test
    void setSpeed_restarts_scheduler_when_running() {
        executor.startIfIdle();
        executor.setSpeed(2);

        verify(scheduler, times(2)).schedule(any(), any());
        verify(taskHandle).cancel();
    }

    @Test
    void startIfIdle_guards_against_paused_or_already_running() {
        executor.startIfIdle();
        executor.startIfIdle();
        verify(scheduler, times(1)).schedule(any(), any());

        executor.pause();
        executor.startIfIdle();
        verify(scheduler, times(1)).schedule(any(), any());
    }

    @Test
    void resume_guards_against_unpaused_state() {
        executor.resume();
        verify(scheduler, never()).schedule(any(), any());
    }

    @Test
    void pause_and_resume_toggle_paused_state() {
        executor.pause();
        executor.resume();

        verify(scheduler).schedule(any(), any());
        assertFalse(executor.isIdle());
    }

    @Test
    void tick_executes_plan_steps_and_finishes() {
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        var future1 = new CompletableFuture<Void>();
        var future2 = CompletableFuture.<Void>completedFuture(null);

        var plan = AnimationPlan.<TestSceneOps>builder()
                .stepAsync(0, ops -> future1)
                .stepAsync(0, ops -> future2)
                .build();

        executor.add(plan);
        executor.startIfIdle();

        verify(scheduler).schedule(runnableCaptor.capture(), any());
        Runnable tickRunnable = runnableCaptor.getValue();

        tickRunnable.run(); // Starts step 0
        future1.complete(null);

        tickRunnable.run(); // Completes step 0 and enters/executes step 1
        tickRunnable.run(); // Evaluates finished plan and stops scheduler

        verify(taskHandle).cancel();
        assertTrue(executor.isIdle());
    }

    @Test
    void tick_handles_step_with_ticks_delay() {
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        var plan = AnimationPlan.<TestSceneOps>builder()
                .step(2, ops -> CompletableFuture.completedFuture(null))
                .build();

        executor.add(plan);
        executor.startIfIdle();

        verify(scheduler).schedule(runnableCaptor.capture(), any());
        Runnable tickRunnable = runnableCaptor.getValue();

        tickRunnable.run(); // Step completed, ticksRemaining set to 2
        tickRunnable.run(); // ticksRemaining decremented to 1
        tickRunnable.run(); // ticksRemaining decremented to 0
        tickRunnable.run(); // Plan finishes and cancels task

        verify(taskHandle).cancel();
        assertTrue(executor.isIdle());
    }

    @Test
    void tick_handles_step_exception_and_cleans_up() {
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        var future = new CompletableFuture<Void>();

        var plan = AnimationPlan.<TestSceneOps>builder()
                .stepAsync(0, ops -> future)
                .build();

        executor.add(plan);
        executor.startIfIdle();

        verify(scheduler).schedule(runnableCaptor.capture(), any());
        Runnable tickRunnable = runnableCaptor.getValue();

        tickRunnable.run();
        future.completeExceptionally(new RuntimeException("Step failure"));

        verify(taskHandle).cancel();
        assertTrue(executor.isIdle());
    }

    @Test
    void tick_ignores_completed_step_when_execution_id_mismatches() {
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        var future = new CompletableFuture<Void>();

        var plan = AnimationPlan.<TestSceneOps>builder()
                .stepAsync(0, ops -> future)
                .build();

        executor.add(plan);
        executor.startIfIdle();

        verify(scheduler).schedule(runnableCaptor.capture(), any());
        Runnable tickRunnable = runnableCaptor.getValue();

        tickRunnable.run();
        executor.onCleanup(); // Increments executionId and cancels task

        assertDoesNotThrow(() -> future.complete(null));
        assertTrue(executor.isIdle());
    }

    @Test
    void tick_returns_early_when_paused_or_waiting_async() {
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        var future = new CompletableFuture<Void>();

        var plan = AnimationPlan.<TestSceneOps>builder()
                .stepAsync(0, ops -> future)
                .build();

        executor.add(plan);
        executor.startIfIdle();

        verify(scheduler).schedule(runnableCaptor.capture(), any());
        Runnable tickRunnable = runnableCaptor.getValue();

        tickRunnable.run(); // waitingForAsyncStep = true
        tickRunnable.run(); // Immediately returns because waitingForAsyncStep is true

        executor.pause();
        tickRunnable.run(); // Immediately returns because paused is true
    }

    @Test
    void onCleanup_resets_executor_state_and_cancels_task() {
        executor.startIfIdle();
        executor.onCleanup();

        verify(taskHandle).cancel();
        assertTrue(executor.isIdle());
    }
}