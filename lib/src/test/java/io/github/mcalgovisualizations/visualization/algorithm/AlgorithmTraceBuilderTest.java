package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.utils.TestContext;
import io.github.mcalgovisualizations.visualization.utils.FirstTestEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTraceBuilderTest {

    @Test
    void getInitialData_returns_current_context_data_snapshot() {
        var context = new TestContext(List.of(1, 2, 3));
        IPlayerSort<TestContext> algorithm = ctx -> {};
        var builder = new AlgorithmTraceBuilder<>(algorithm, context);

        assertEquals(List.of(1, 2, 3), builder.getInitialData());
    }

    @Test
    void build_executes_algorithm_on_copied_context_and_returns_trace() {
        var context = new TestContext(List.of(1, 2, 3));
        IPlayerSort<TestContext> algorithm = ctx -> {
            ctx.setData(List.of(4, 5, 6));
            ctx.addEvent(new FirstTestEvent(1));
        };

        var builder = new AlgorithmTraceBuilder<>(algorithm, context);
        var trace = builder.build();

        assertEquals(List.of(1, 2, 3), trace.initialData());
        assertEquals(List.of(4, 5, 6), trace.finalData());
        assertEquals(1, trace.history().size());
        assertEquals(new FirstTestEvent(1), trace.history().getFirst());
        assertEquals(List.of(1, 2, 3), context.copyData());
        assertTrue(context.getEvents().isEmpty());
    }

    @Test
    void randomizeAndBuild_randomizes_source_data_and_creates_trace() {
        var context = new TestContext(List.of(1, 2, 3));
        IPlayerSort<TestContext> algorithm = ctx -> ctx.addEvent(new FirstTestEvent(99));

        var builder = new AlgorithmTraceBuilder<>(algorithm, context);
        var trace = builder.randomizeAndBuild();

        assertEquals(List.of(99), trace.initialData());
        assertEquals(List.of(99), trace.finalData());
        assertEquals(1, trace.history().size());
    }

    @Test
    void algorithmTrace_record_getters_and_state() {
        var event = new FirstTestEvent(5);
        var trace = new AlgorithmTraceBuilder.AlgorithmTrace<>(List.of(1), List.of(2), List.of(event));

        assertEquals(List.of(1), trace.initialData());
        assertEquals(List.of(2), trace.finalData());
        assertEquals(1, trace.history().size());
        assertEquals(event, trace.history().getFirst());
    }
}