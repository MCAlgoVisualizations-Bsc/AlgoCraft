package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.CellState;
import io.github.mcalgovisualizations.visualization.algorithms.events.CellStateTransition;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;

import java.util.List;

/**
 * Compile-safe regression helper for checking that A* does not emit terrain bootstrap paint events.
 * This module currently includes src/test in main sources.
 */
public final class PlayerAStarTest {

    private PlayerAStarTest() {
    }

    public static boolean hasBootstrapTerrainPaintEvents() {
        var algorithm = new PlayerAStar(3);
        var values = new SortingCollection<>(List.of(
                new Data<>(2), new Data<>(0), new Data<>(0),
                new Data<>(1), new Data<>(1), new Data<>(0),
                new Data<>(0), new Data<>(0), new Data<>(3)
        ));

        algorithm.sort(values);

        return values.events().stream()
                .filter(CellStateTransition.class::isInstance)
                .map(CellStateTransition.class::cast)
                .anyMatch(transition ->
                        transition.first() == CellState.DEFAULT &&
                                (transition.second() == CellState.WALL
                                        || transition.second() == CellState.START
                                        || transition.second() == CellState.GOAL)
                );
    }
}
