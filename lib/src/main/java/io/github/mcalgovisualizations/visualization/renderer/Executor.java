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
 * <p>The executor advances one plan step at a time using Minestom's scheduler. It
 * waits between steps according to each step's tick delay, and it can also bridge
 * asynchronous scene operations without blocking the server thread.</p>
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
    private boolean waitingForAsyncStep = false;
    private int executionId = 0;

    private final Queue<AnimationPlan<O>> queue = new LinkedList<>();

    private AnimationPlan<O> currentPlan = null;
    private int stepIndex = 0;
    private int ticksRemaining = 0;
    private boolean stepJustEntered = false;

    private boolean paused = false;

    private int speed = 1;

    /**
     * Creates a new executor for a specific scene.
     *
     * @param scene the scene that animation plans will act on
     */
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
     * Starts the scheduler if the executor is not paused and no scheduler task is running.
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
     * <p>The current plan, step index, remaining wait time, and queued plans are preserved.</p>
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
     * Returns whether this executor has no active scheduler, no active plan, and no queued plans.
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
        if (waitingForAsyncStep) return;

        int opsThisTick = 0;

        while (opsThisTick < MAX_OPS_PER_TICK) {
            if (waitingForAsyncStep) return;

            if (ticksRemaining > 0) {
                ticksRemaining--;
                return;
            }

            if (currentPlan == null) {
                currentPlan = queue.poll();
                stepIndex = 0;
                stepJustEntered = true;

                if (currentPlan == null) {
                    stopScheduler();
                    return;
                }
            }

            if (stepIndex >= currentPlan.steps().size()) {
                finishCurrentPlan();
                continue;
            }

            if (stepJustEntered) {
                AnimationPlan.Step<O> step = currentPlan.steps().get(stepIndex);

                waitingForAsyncStep = true;
                stepJustEntered = false;

                final int currentExecutionId = executionId;
                step.run(scene).whenComplete((_, throwable) -> {
                    if (currentExecutionId != executionId) return;

                    if (throwable != null) {
                        throwable.printStackTrace();
                        onCleanup();
                        return;
                    }

                    ticksRemaining = step.ticks();
                    stepIndex++;
                    stepJustEntered = true;
                    waitingForAsyncStep = false;
                });

                opsThisTick++;
                return;
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
     * <p>This should be called when the owning scene or session is being destroyed.</p>
     */
    public void onCleanup() {
        paused = false;
        stopScheduler();
        currentPlan = null;
        stepIndex = 0;
        ticksRemaining = 0;
        stepJustEntered = false;
        waitingForAsyncStep = false;
        executionId++;
        queue.clear();
    }
}
