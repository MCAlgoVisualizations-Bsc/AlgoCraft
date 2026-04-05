package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.PlayerControls;
import io.github.mcalgovisualizations.visualization.algorithms.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import io.github.mcalgovisualizations.visualization.ui.PlayerFeedback;
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
    private Task runningTask = null;
    private State state = State.NEW;

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
        assertNotCleared();

        var model = stepper.getBackingCollection();
        renderer.initialize(model);
        state = State.INITIALIZED;
    }

    @Override
    public void start() {
        if (state == State.RUNNING) return;

        if(state == State.PAUSED) {
            resume();
            return;
        }

        if (state != State.INITIALIZED) {
            throw new IllegalStateException("VisualizationController must be initialized or paused before starting");
        }

        renderer.resume();
        scheduleSteppingTask();
        state = State.RUNNING;
        audience.start();
    }

    public void resume() {
        if (state != State.PAUSED) {
            throw new IllegalStateException("VisualizationController is not paused");
        }

        renderer.resume();
        scheduleSteppingTask();
        state = State.RUNNING;
    }

    @Override
    public void pause() {
        if (state == State.CLEARED) return;
        if (state == State.PAUSED) return;

        cancelRunningTask();
        renderer.pause();
        audience.pause();

        if (state != State.COMPLETED) {
            state = State.PAUSED;
        }
    }

    @Override
    public void step() {
        assertSteppable();

        if (stepper.isComplete()) {
            completeVisualization();
            return;
        }

        final IAlgorithmEvent event = stepper.step();
        if (event != null) {
            renderer.render(event);
            audience.step();
        }

        if (stepper.isComplete()) {
            completeVisualization();
        }
    }

    @Override
    public void back() {
        if (state == State.NEW || state == State.CLEARED) {
            throw new IllegalStateException("VisualizationController is not initialized");
        }

        final IAlgorithmEvent event = stepper.back();
        if (event != null) {
            renderer.render(event);
            audience.back();
        }

        if (state == State.COMPLETED) {
            state = State.PAUSED;
        }
    }

    private void scheduleSteppingTask() {
        cancelRunningTask();

        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::autoStep)
                .repeat(Duration.ofMillis(this.ticksPerStep * 50L))
                .schedule();
    }

    private void autoStep() {
        if (state != State.RUNNING) {
            cancelRunningTask();
            return;
        }

        if (renderer.hasPendingAnimations()) {
            return;
        }

        if (stepper.isComplete()) {
            completeVisualization();
            return;
        }

        step();
    }

    public void setSpeed(int ticksPerStep) {
        this.ticksPerStep = Math.max(1, ticksPerStep);

        if (state == State.RUNNING) {
            scheduleSteppingTask();
        }
    }

    @Override
    public void clear() {
        cancelRunningTask();
        renderer.onCleanup();
        stepper.onCleanup();
        audience.clear();
        state = State.CLEARED;
    }

    @Override
    public void randomize() {
        cancelRunningTask();

        renderer.onCleanup();
        var layout = stepper.randomizeCollection(24);
        renderer.initialize(layout);
        audience.randomize();
        state = State.INITIALIZED;
    }

    @Override
    public String toString() {
        return stepper.getAlgoName();
    }

    // private helpers to ensure state transitions are correct
    private void completeVisualization() {
        cancelRunningTask();
        renderer.complete();
        state = State.COMPLETED;
    }

    private void cancelRunningTask() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

    private void assertSteppable() {
        if (state == State.NEW || state == State.CLEARED) {
            throw new IllegalStateException("VisualizationController is not initialized");
        }
    }

    private void assertNotCleared() {
        if (state == State.CLEARED) {
            throw new IllegalStateException("VisualizationController has been cleared");
        }
    }
}