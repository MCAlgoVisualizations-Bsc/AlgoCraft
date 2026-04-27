package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class PlayerBSTSearch<I extends Comparable<I>> implements IPlayerSort<NodeContext<I>> {

    @Override
    public void run(NodeContext<I> context) {
        // This is no longer used, as we moved to PlayerGraphSearch.
        // It's left here to avoid build issues.
    }
}
