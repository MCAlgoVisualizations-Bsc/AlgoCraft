package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.PlayerControls;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.ui.PlayerFeedback;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A controller of time so forwards, back, adjusting speed belongs here.
 */
public class VisualizationController<T extends Comparable<T>> implements PlayerControls {
    private enum State {
        NEW,
        INITIALIZED,
        RUNNING,
        PAUSED,
        COMPLETED,
        CLEARED
    }

    private final AlgorithmStepper<T> stepper;
    private final Renderer renderer;
    private final PlayerFeedback audience;

    private int ticksPerStep = 5;
    private boolean IS_RUNNING = false;
    private Task runningTask = null;
    private boolean IS_INITIALIZED = false;

    private State state;

    public VisualizationController(
            @NotNull IPlayerSort algorithm,
            @NotNull Renderer renderer,
            @NotNull ISort<T> collection,
            @NotNull PlayerFeedback audience
    ) {
        this.stepper = new AlgorithmStepper<>(algorithm, collection);
        this.renderer = renderer;
        this.audience = audience;
    }

    public void startVisualization() {
        var model = stepper.getBackingCollection();
        renderer.initialize(model);
        this.state = State.INITIALIZED;
    }

    @Override
    public void start() {
        if (IS_RUNNING) return;

        renderer.resume();
        IS_RUNNING = true;
        scheduleSteppingTask();
        audience.start();

    }

    @Override
    public void resume() {
        this.state = State.RUNNING;

        start();
        audience.resume();

    }

    @Override
    public void stop() {
        IS_RUNNING = false;

        if(runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        renderer.stop();
        audience.stop();

        this.state = State.PAUSED;
    }

    @Override
    public void step() {
        final IAlgorithmEvent event = stepper.step();
        renderer.render(event);
        audience.step();

        this.state = State.RUNNING;
    }

    @Override
    public void back() {
        final IAlgorithmEvent event = stepper.back();
        renderer.render(event);
        audience.back();

        this.state = State.RUNNING;
    }

    private void scheduleSteppingTask() {
        if (runningTask != null) {
            runningTask.cancel();
        }

        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::autoStep)
                .repeat(Duration.ofMillis(this.ticksPerStep * 50L))
                .schedule();
    }

    private void autoStep() {
        if (renderer.hasPendingAnimations()) {
            return;
        }

        if (stepper.isComplete()) {
            renderer.complete();
            return;
        }
        step();
    }

    public void setSpeed(int ticksPerStep) {
        this.ticksPerStep = Math.max(1, ticksPerStep);
        if (IS_RUNNING) {
            scheduleSteppingTask();
        }
    }

    @Override
    public void clear() {
        stop();
        this.renderer.onCleanup();
        this.stepper.onCleanup();
        audience.clear();

        this.state = State.CLEARED;
    }

    @Override
    public void randomize() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        IS_RUNNING = false;

        renderer.onCleanup();
        var layout = this.stepper.randomizeCollection(24);
        this.renderer.initialize(layout);
        audience.randomize();

        this.state = State.INITIALIZED;
    }

    @Override
    public String toString() {
        return stepper.getAlgoName();
    }

    private void assertInitialized() {
        if (!state.equals(State.INITIALIZED)) {
            throw new IllegalStateException("VisualizationController not initialized");
        }
    }

    private void assertRunning() {
        if (!state.equals(State.RUNNING)) {
            throw new IllegalStateException("VisualizationController not running");
        }
    }
}