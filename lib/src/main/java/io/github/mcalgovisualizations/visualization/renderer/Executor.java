package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

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

    private int SPEED = 1;

    public Executor(O scene) {
        this.scene = Objects.requireNonNull(scene, "scene");
    }

    public void add(@NotNull AnimationPlan<O> plan) {
        if (plan.isEmpty()) return;
        queue.add(plan);
    }

    public void startIfIdle() {
        if (paused) return;
        if (runningTask != null) return;

        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::tick)
                .repeat(Duration.ofMillis(SPEED * 50L))
                .schedule();
    }

    public void pause() {
        paused = true;
        stopScheduler();
    }

    public void resume() {
        if (!paused) return;
        paused = false;
        startIfIdle();
    }

    public boolean isIdle() {
        return currentPlan == null && queue.isEmpty() && runningTask == null;
    }

    public void setSpeed(int speed) {
        if (speed <= 0) throw new IllegalArgumentException("speed must be > 0");
        this.SPEED = speed;

        if (runningTask != null) {
            stopScheduler();
            startIfIdle();
        }
    }

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

    private void finishCurrentPlan() {
        currentPlan = null;
        stepIndex = 0;
        ticksRemaining = 0;
        stepJustEntered = false;

        if (queue.isEmpty()) stopScheduler();
    }

    private void stopScheduler() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

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
