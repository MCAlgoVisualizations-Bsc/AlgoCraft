package io.github.mcalgovisualizations.visualization.algorithm;

/**
 * Marker interface representing a single step or action produced by an algorithm.
 *
 * <p>Implementations should be immutable and describe a discrete, replayable event
 * in the algorithm's execution (e.g. comparison, swap, insertion).</p>
 *
 * <p>These events are consumed by a renderer that
 * interprets them to produce visual changes.</p>
 *
 * <p>No behavior is defined here; this interface exists to provide a common type
 * for heterogeneous algorithm events.</p>
 *
 * <p><strong>Implementation Requirements:</strong></p>
 * <ul>
 *     <li>Be immutable (no internal state changes after creation)</li>
 *     <li>Contain all data required for rendering or interpretation</li>
 *     <li>Avoid referencing external mutable state</li>
 * </ul>
 */
public interface IAlgorithmEvent { }
