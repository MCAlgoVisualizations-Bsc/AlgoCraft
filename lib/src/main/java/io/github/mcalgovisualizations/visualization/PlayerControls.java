package io.github.mcalgovisualizations.visualization;

/**
 * Defines control actions for a visualization or playback system.
 * <p>
 * Implementations translate these high-level control signals into
 * concrete behavior (e.g., updating state, triggering animations,
 * or providing user feedback).
 */
public interface PlayerControls {

    /**
     * Triggers randomization of layout.
     */
    void randomize();

    /**
     * Starts or initializes execution.
     */
    void start();

    /**
     * Stops or halts execution.
     */
    void stop();

    /**
     * Advances execution by a single step.
     */
    void step();

    /**
     * Moves execution one step backward.
     */
    void back();

    /**
     * Resumes execution after being paused or stopped.
     */
    void resume();

    /**
     * Clears the visualization.
     */
    void clear();
}