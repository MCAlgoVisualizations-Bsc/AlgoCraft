package io.github.mcalgovisualizations.visualization.engine;

/**
 * Defines control actions for a visualization playback system.
 *
 * <p>Implementations translate these high-level control signals into state changes,
 * animation control, and user feedback.</p>
 */
public interface PlayerControls {

    /**
     * Regenerates the visualization from a randomized starting state.
     */
    void randomize();

    /**
     * Starts or resumes automatic playback.
     */
    void start();

    /**
     * Pauses automatic playback.
     */
    void pause();

    /**
     * Advances execution by a single step.
     */
    void step();

    /**
     * Moves execution one step backward.
     */
    void back();

    /**
     * Clears the visualization.
     */
    void clear();

    /**
     * Cycles the playback speed.
     *
     * @return the new speed value, where smaller values mean faster playback
     */
    int changeSpeed();
}
