package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * Executes queued {@link AnimationPlan}s against a scene over time.
 *
 * <p>The executor processes animation plans step by step using Minestom's scheduler.
 * Each step applies an operation to the scene and may wait a configured number of
 * ticks before the next step is executed.</p>
 *
 * <p>Zero-wait steps are processed in the same tick, up to {@link #MAX_OPS_PER_TICK},
 * to avoid infinite loops or excessive work in a single server tick.</p>
 *
 * @param <O> the type of scene operations this executor can apply
 */
public final class Executor<O extends ISceneOps> {

    private static final int MAX_OPS_PER_TICK = 256;

    private final O scene;
    private Task runningTask = null;

    private final Queue<AnimationPlan<O>> queue = new LinkedList<>();

    private AnimationPlan<O> currentPlan = null;
    private int stepIndex = 0;
    private int ticksRemaining = 0;
    private boolean stepJustEntered = false;

    private boolean paused = false;

    private int speed = 1;

    public Executor(O scene) {
        this.scene = Objects.requireNonNull(scene, "scene");
    }

    /**
     * Adds an animation plan to the execution queue.
     *
     * <p>Empty plans are ignored.</p>
     *
     * @param plan the plan to enqueue
     */
    public void add(@NotNull AnimationPlan<O> plan) {
        if (plan.isEmpty()) return;
        queue.add(plan);
    }

    /**
     * Starts the scheduler if the executor is not paused and no scheduler task is
     * currently running.
     */
    public void startIfIdle() {
        if (paused) return;
        if (runningTask != null) return;

        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::tick)
                .repeat(Duration.ofMillis(speed * 50L))
                .schedule();
    }

    /**
     * Pauses execution and stops the scheduler.
     *
     * <p>The current plan, step index, remaining wait time, and queued plans are
     * preserved.</p>
     */
    public void pause() {
        paused = true;
        stopScheduler();
    }

    /**
     * Resumes execution if currently paused.
     */
    public void resume() {
        if (!paused) return;
        paused = false;
        startIfIdle();
    }

    /**
     * Returns whether this executor has no active scheduler, no active plan, and no
     * queued plans.
     *
     * @return {@code true} if there is no work currently running or queued
     */
    public boolean isIdle() {
        return currentPlan == null && queue.isEmpty() && runningTask == null;
    }

    /**
     * Sets the scheduler interval multiplier.
     *
     * <p>A value of {@code 1} runs every server tick, {@code 2} runs every two ticks,
     * and so on.</p>
     *
     * @param speed scheduler interval multiplier; must be greater than {@code 0}
     * @throws IllegalArgumentException if {@code speed <= 0}
     */
    public void setSpeed(int speed) {
        if (speed <= 0) throw new IllegalArgumentException("speed must be > 0");
        this.speed = speed;

        if (runningTask != null) {
            stopScheduler();
            startIfIdle();
        }
    }

    /**
     * Processes animation work for one scheduler execution.
     *
     * <p>This method advances the current plan until it either reaches a step with a
     * positive wait duration, runs out of work, or reaches {@link #MAX_OPS_PER_TICK}
     * operations for this tick.</p>
     */
    private void tick() {
        if (paused) return;

        int opsThisTick = 0;
        while (opsThisTick < MAX_OPS_PER_TICK) {
            // If we're waiting inside a step, consume one tick and return.
            if (ticksRemaining > 0) {
                ticksRemaining--;
                return;
            }

            // Ensure we have a plan.
            if (currentPlan == null) {
                currentPlan = queue.poll();
                stepIndex = 0;
                stepJustEntered = true;

                if (currentPlan == null) {
                    stopScheduler();
                    return;
                }
            }

            // Finished plan?
            if (stepIndex >= currentPlan.steps().size()) {
                finishCurrentPlan();
                continue;
            }

            if (stepJustEntered) {
                AnimationPlan.Step<O> step = currentPlan.steps().get(stepIndex);
                step.op().accept(scene);
                ticksRemaining = step.ticks();

                stepJustEntered = false;
                stepIndex++;
                stepJustEntered = true;
                opsThisTick++;

                // Stay in the loop to consume zero-wait steps immediately.
                if (ticksRemaining > 0) {
                    return;
                }
            }
        }

    }

    /**
     * Clears the active plan state and stops the scheduler if there is no queued work.
     */
    private void finishCurrentPlan() {
        currentPlan = null;
        stepIndex = 0;
        ticksRemaining = 0;
        stepJustEntered = false;

        if (queue.isEmpty()) stopScheduler();
    }

    /**
     * Cancels the active scheduler task, if one exists.
     */
    private void stopScheduler() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

    /**
     * Stops execution and clears all queued and active animation state.
     *
     * <p>This should be called when the owning scene/session is being destroyed.</p>
     */
    public void onCleanup() {
        paused = false;
        stopScheduler();
        currentPlan = null;
        stepIndex = 0;
        ticksRemaining = 0;
        stepJustEntered = false;
        queue.clear();
    }
}
