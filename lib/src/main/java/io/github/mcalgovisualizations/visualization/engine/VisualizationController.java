package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A controller of time so forwards, back, adjusting speed belongs here.
 */
public class VisualizationController {
    private final AlgorithmStepper stepper;
    private final Renderer renderer;

    private int ticksPerStep = 5;
    private boolean IS_RUNNING = false;
    private Task runningTask = null;
    private boolean IS_INITIALIZED = false;

    public VisualizationController(
            @NotNull IPlayerSort algorithm,
            @NotNull Renderer renderer,
            @NotNull SortingCollection<?> collection
    ) {
        this.stepper = new AlgorithmStepper<>(algorithm, collection);
        this.renderer = renderer;
    }

    public void setAudience(Audience audience) {
        renderer.setAudience(audience);
    }

    @SuppressWarnings("unchecked")
    public void startVisualization() {
        var event = stepper.getBackingCollection();
        renderer.initialize(event);
        this.IS_INITIALIZED = true;
    }

    public void start() {
        if (!IS_INITIALIZED) throw new IllegalStateException("VisualizationController not initialized");
        if (IS_RUNNING) return;

        renderer.resume();
        IS_RUNNING = true;
        scheduleSteppingTask();
    }

    public void resume() {
        start();
    }

    public void stop() {
        IS_RUNNING = false;
        if(runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        renderer.stop();
    }

    public void step() {
        final var event = stepper.step();
        renderer.render(event);
    }

    public void back() {
        final var event = stepper.back();
        renderer.render(event);
    }

    private void scheduleSteppingTask() {
        if (runningTask != null) {
            runningTask.cancel();
        }

        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::step)
                .repeat(Duration.ofMillis(this.ticksPerStep * 50L))
                .schedule();
    }

    public void setSpeed(int ticksPerStep) {
        this.ticksPerStep = Math.max(1, ticksPerStep);
        if (IS_RUNNING) {
            scheduleSteppingTask();
        }
    }

    public void cleanup() {
        stop();
        this.renderer.onCleanup();
        this.stepper.onCleanup();
    }

    @SuppressWarnings("unchecked")
    public void randomize() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        IS_RUNNING = false;

        var layout = this.stepper.randomizeCollection(24);
        this.renderer.initialize(layout);
    }

    @Override
    public String toString() {
        return stepper.getAlgoName();
    }
}