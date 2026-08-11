package io.github.mcalgovisualizations.visualization.renderer;

import io.github.mcalgovisualizations.visualization.TaskScheduler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public final class Executor<O extends ISceneOps> {

    private static final int MAX_OPS_PER_TICK = 256;

    private final O scene;
    private final TaskScheduler scheduler;

    private TaskScheduler.TaskHandle runningTask = null;
    private boolean waitingForAsyncStep = false;
    private int executionId = 0;

    private final Queue<AnimationPlan<O>> queue = new LinkedList<>();

    private AnimationPlan<O> currentPlan = null;
    private int stepIndex = 0;
    private int ticksRemaining = 0;
    private boolean stepJustEntered = false;

    private boolean paused = false;

    private int speed = 1;

    public Executor(O scene, TaskScheduler scheduler) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public Executor(O scene) {
        this(scene, (runnable, period) -> {
            var task = MinecraftServer.getSchedulerManager()
                    .buildTask(runnable)
                    .repeat(period)
                    .schedule();
            return task::cancel;
        });
    }

    public void add(@NotNull AnimationPlan<O> plan) {
        if (plan.isEmpty()) return;
        queue.add(plan);
    }

    public void startIfIdle() {
        if (paused) return;
        if (runningTask != null) return;

        runningTask = scheduler.schedule(this::tick, Duration.ofMillis(speed * 50L));
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
        this.speed = speed;

        if (runningTask != null) {
            stopScheduler();
            startIfIdle();
        }
    }

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
        waitingForAsyncStep = false;
        executionId++;
        queue.clear();
    }
}