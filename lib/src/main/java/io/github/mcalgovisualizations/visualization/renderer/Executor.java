package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public final class Executor {

    private final ISceneOps scene;
    private Task runningTask = null;

    private final Queue<AnimationPlan> queue = new LinkedList<>();

    private AnimationPlan currentPlan = null;
    private int stepIndex = 0;
    private int ticksRemaining = 0;
    private boolean stepJustEntered = false;

    private boolean paused = false;

    private int SPEED = 1;

    public Executor(ISceneOps scene) {
        this.scene = Objects.requireNonNull(scene, "scene");
    }

    public void add(@NotNull AnimationPlan plan) {
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

        // If we're waiting inside a step, consume one tick and return
        if (ticksRemaining > 0) {
            ticksRemaining--;
            return;
        }

        // Ensure we have a plan
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
            return;
        }

        // Enter new step: apply op ONCE, then set wait
        if (stepJustEntered) {
            var step = currentPlan.steps().get(stepIndex);

            // This is the whole point:
            step.op().accept(scene);

            // Wait AFTER applying (0 means continue next tick)
            ticksRemaining = step.ticks();

            stepJustEntered = false;

            // Advance to next step once this step's wait has elapsed
            stepIndex++;
            stepJustEntered = true;

            // If ticksRemaining == 0, we could loop and apply multiple 0-tick steps
            // in one scheduler tick — optional. Keep simple for now.
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
