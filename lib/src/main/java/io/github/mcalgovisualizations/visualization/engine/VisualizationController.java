package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.PlayerControls;
import io.github.mcalgovisualizations.visualization.ui.PlayerFeedback;
import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A controller of time so forwards, back, adjusting speed belongs here.
 */
public class VisualizationController implements PlayerControls {
    private final AlgorithmStepper stepper;
    private final Renderer renderer;


    private final PlayerFeedback audience;


    private int ticksPerStep = 5;
    private boolean IS_RUNNING = false;
    private Task runningTask = null;
    private boolean IS_INITIALIZED = false;

    public VisualizationController(
            @NotNull IPlayerSort algorithm,
            @NotNull Renderer renderer,
            @NotNull SortingCollection<?> collection,
            @NotNull PlayerFeedback audience
    ) {
        this.stepper = new AlgorithmStepper<>(algorithm, collection);
        this.renderer = renderer;
        this.audience = audience;
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
        audience.start();
    }

    public void resume() {
        start();
        audience.resume();
    }

    public void stop() {
        IS_RUNNING = false;
        if(runningTask != null) {
            runningTask.cancel();
            runningTask = null;
        }
        renderer.stop();
        audience.stop();
    }

    public void step() {
        final IAlgorithmEvent event = stepper.step();
        renderer.render(event);

        if (!(event instanceof NoOp)) {
            audience.step();
        }
    }

    public void back() {
        final IAlgorithmEvent event = stepper.back();
        renderer.render(event);
        if (!(event instanceof NoOp)) {
            audience.back();
        }
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
        step();
    }

    public void setSpeed(int ticksPerStep) {
        this.ticksPerStep = Math.max(1, ticksPerStep);
        if (IS_RUNNING) {
            scheduleSteppingTask();
        }
    }

    public void clear() {
        stop();
        this.renderer.onCleanup();
        this.stepper.onCleanup();
        audience.clear();
    }

    @SuppressWarnings("unchecked")
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
    }

    @Override
    public String toString() {
        return stepper.getAlgoName();
    }
}