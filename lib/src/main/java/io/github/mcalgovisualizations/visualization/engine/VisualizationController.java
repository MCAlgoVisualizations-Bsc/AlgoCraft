package io.github.mcalgovisualizations.visualization.engine;

import io.github.mcalgovisualizations.visualization.algorithms.IPlayerSort;
import io.github.mcalgovisualizations.visualization.algorithms.AlgorithmStepper;
import io.github.mcalgovisualizations.visualization.algorithms.events.Complete;
import io.github.mcalgovisualizations.visualization.algorithms.events.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.algorithms.events.NoOp;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import io.github.mcalgovisualizations.visualization.renderer.Renderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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
    private Audience audience = Audience.empty();

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

    public void addAudience(Audience player) {
        this.audience = Audience.audience(player); // TODO: Create an audience class such controller and renderer can share
        renderer.setAudience(player);
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
        playUiSound("minecraft:block.note_block.chime", 1.0f, 1.25f);
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
        playUiSound("minecraft:block.note_block.bass", 0.9f, 0.9f);
    }

    public void step() {
        final IAlgorithmEvent event = stepper.step();
        renderer.render(event);

        if (event instanceof Complete) {
            return;
        }
        if (!(event instanceof NoOp)) {
            playUiSound("minecraft:block.note_block.hat", 0.6f, 1.6f);
        }
    }

    public void back() {
        final IAlgorithmEvent event = stepper.back();
        renderer.render(event);
        if (!(event instanceof NoOp)) {
            playUiSound("minecraft:block.note_block.snare", 0.7f, 1.2f);
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
        playUiSound("minecraft:entity.item.pickup", 0.8f, 1.3f);
    }

    private void playUiSound(String key, float volume, float pitch) {
        audience.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, volume, pitch));
    }

    @Override
    public String toString() {
        return stepper.getAlgoName();
    }
}