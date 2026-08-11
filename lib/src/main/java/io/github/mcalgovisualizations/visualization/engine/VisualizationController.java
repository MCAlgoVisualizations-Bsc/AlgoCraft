package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.algorithm.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.algorithm.AlgorithmTraceBuilder;
import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import io.github.mcalgovisualizations.visualization.instance.PlayerFeedback;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinates trace playback, stepping, speed changes, and user feedback for a visualization session.
 *
 * <p>This controller owns the session state machine and bridges the trace builder,
 * renderer, and player feedback layer.</p>
 */
public class VisualizationController<I, C extends AlgorithmContext<I>> implements PlayerControls {
    private final AlgorithmTraceBuilder<I, C> traceBuilder;
    private AlgorithmStepper algorithmStepper;
    private final Renderer<I, ?> renderer;
    private final PlayerFeedback audience;

    private static final int MIN_TICKS_PER_STEP = 1;
    private static final int MAX_TICKS_PER_STEP = 5;


    private int delayPerStep = MAX_TICKS_PER_STEP;
    private Task runningTask = null;
    private ControllerState state = ControllerState.NEW;

    /**
     * Creates a new visualization controller.
     *
     * @param renderer the renderer that executes animation events
     * @param traceBuilder the trace builder used to rebuild the playback state
     * @param audience the player feedback bridge
     */
    public VisualizationController(
            @NotNull Renderer<I, ?> renderer,
            @NotNull AlgorithmTraceBuilder<I, C> traceBuilder,
            @NotNull PlayerFeedback audience
    ) {
        this.traceBuilder = traceBuilder;
        this.algorithmStepper = new AlgorithmStepper(traceBuilder.build().history());
        this.renderer = renderer;
        this.audience = audience;
    }

    /**
     * Builds the initial trace and initializes the renderer with the first model snapshot.
     *
     * @return a future that completes when initialization finishes
     */
    public CompletableFuture<Void> startVisualization() throws NullPointerException {
        assertNotCleared();
        applyPlaybackSpeed();
        state = ControllerState.INITIALIZED;
        return renderer.initialize(traceBuilder.getInitialData());
    }

    /**
     * Starts automatic playback or resumes it if the controller is currently paused.
     */
    @Override
    public void start() {
        if (state == ControllerState.RUNNING) return;

        if(state == ControllerState.PAUSED) {
            resume();
            return;
        }

        if (state == ControllerState.COMPLETED) {
            // Allow START to act as resume if playback was paused during completion.
            renderer.resume();
            audience.start();
            return;
        }

        if (state != ControllerState.INITIALIZED) {
            throw new IllegalStateException("VisualizationController must be initialized, paused, or completed before starting: " + state);
        }

        renderer.resume();
        scheduleSteppingTask();
        state = ControllerState.RUNNING;
        audience.start();
    }

    /**
     * Resumes automatic playback from the paused state.
     */
    public void resume() {
        if (state != ControllerState.PAUSED) {
            throw new IllegalStateException("VisualizationController is not paused");
        }

        renderer.resume();
        scheduleSteppingTask();
        state = ControllerState.RUNNING;
    }

    /**
     * Pauses automatic playback and cancels the scheduler task.
     */
    @Override
    public void pause() {
        if (state == ControllerState.CLEARED) return;
        if (state == ControllerState.PAUSED) return;

        cancelRunningTask();
        renderer.pause();
        audience.pause();

        if (state != ControllerState.COMPLETED) {
            state = ControllerState.PAUSED;
        }
    }

    /**
     * Advances playback by a single event.
     */
    @Override
    public void step() {
        assertSteppable();

        if (algorithmStepper.isComplete()) {
            completeVisualization();
            return;
        }

        final IAlgorithmEvent event = algorithmStepper.step();
        if (event != null) {
            renderer.render(event);
            audience.step();
        }

        if (algorithmStepper.isComplete()) {
            completeVisualization();
        }
    }

    /**
     * Steps playback backwards by one event.
     */
    @Override
    public void back() {
        if (state == ControllerState.NEW || state == ControllerState.CLEARED) {
            throw new IllegalStateException("VisualizationController is not initialized");
        }

        final IAlgorithmEvent event = algorithmStepper.back();
        if (event != null) {
            renderer.render(event);
            audience.back();
        }

        if (state == ControllerState.COMPLETED) {
            state = ControllerState.PAUSED;
        }
    }


    private void scheduleSteppingTask() {
        cancelRunningTask();
        final int schedulerTicks = Math.max(MIN_TICKS_PER_STEP, delayPerStep);
        runningTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::autoStep)
                .repeat(Duration.ofMillis(schedulerTicks * 50L))
                .schedule();
    }

    private void autoStep() {
        if (state != ControllerState.RUNNING) {
            cancelRunningTask();
            return;
        }

        if (renderer.hasPendingAnimations()) {
            return;
        }

        if (algorithmStepper.isComplete()) {
            completeVisualization();
            return;
        }

        step();
    }

    /**
     * Cycles the playback speed between the configured tick values.
     *
     * @return the new tick delay per step
     */
    @Override
    public int changeSpeed() {
        // Lower ticks/step means faster stepping plus faster animation playback.
        int nextSpeed = this.delayPerStep - 1;
        if (nextSpeed < MIN_TICKS_PER_STEP) {
            nextSpeed = MAX_TICKS_PER_STEP;
        }
        setSpeed(nextSpeed);
        audience.sendMessage(Component.text("Ticks/step: " + this.delayPerStep));
        return this.delayPerStep;
    }

    private void setSpeed(int ticksPerStep) {
        this.delayPerStep = Math.clamp(ticksPerStep, MIN_TICKS_PER_STEP, MAX_TICKS_PER_STEP);
        applyPlaybackSpeed();
        if (state == ControllerState.RUNNING) {
            scheduleSteppingTask();
        }
    }

    private void applyPlaybackSpeed() {
        int schedulerTicks = Math.max(MIN_TICKS_PER_STEP, delayPerStep);
        renderer.setSpeed(schedulerTicks);
    }

    /**
     * Clears the current visualization state and resets playback.
     */
    @Override
    public void clear() {
        cancelRunningTask();
        renderer.onCleanup();
        audience.clear();
        state = ControllerState.CLEARED;
    }

    /**
     * Regenerates the trace from a randomized starting model.
     */
    @Override
    public void randomize() {
        cancelRunningTask();
        renderer.onCleanup();

        final var trace = traceBuilder.randomizeAndBuild();
        
        this.algorithmStepper = new AlgorithmStepper(trace.history());
        renderer.initialize(trace.initialData());
        audience.randomize();
        state = ControllerState.INITIALIZED;
    }

    // private helpers to ensure state transitions are correct
    private void completeVisualization() {
        cancelRunningTask();
        renderer.complete();
        state = ControllerState.COMPLETED;
    }

    private void cancelRunningTask() {
        if (runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
    }

    private void assertSteppable() {
        if (state == ControllerState.NEW || state == ControllerState.CLEARED) {
            throw new IllegalStateException("VisualizationController is not initialized");
        }
    }

    private void assertNotCleared() {
        if (state == ControllerState.CLEARED) {
            throw new IllegalStateException("VisualizationController has been cleared");
        }
    }
}
