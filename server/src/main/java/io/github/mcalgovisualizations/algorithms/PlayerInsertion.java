package io.github.mcalgovisualizations.algorithms;

import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

public class PlayerInsertion<I extends Comparable<I>> implements IPlayerSort<SortingContext<I>> {

    @Override
    public void run(SortingContext<I> ctx) {
        var values = ctx.getData();
        int n = values.size();
        for (int i = 1; i < n; i++) {
            int j = i;

            var x = values.get(j);
            var y = values.get(j - 1);


            ctx.emit(new Compare(j, j-1, y, x));
            while (j > 0 && x.compareTo(y) < 0) {
                ctx.emit(new Swap(j, j - 1, y, x));
                ctx.swap(j, j - 1);
                j--;
            }
        }
    }

}