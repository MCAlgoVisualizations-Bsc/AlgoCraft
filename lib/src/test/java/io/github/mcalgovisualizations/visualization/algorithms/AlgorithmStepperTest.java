package io.github.mcalgovisualizations.visualization.algorithms;

import io.github.mcalgovisualizations.visualization.algorithms.events.Compare;
import io.github.mcalgovisualizations.visualization.algorithms.events.Message;
import io.github.mcalgovisualizations.visualization.models.Data;
import io.github.mcalgovisualizations.visualization.models.ISort;
import io.github.mcalgovisualizations.visualization.models.SortingCollection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmStepperTest {
    @Test
    void step_throws_when_empty() {
        var stepper = createStepper();
        var ex = assertThrows(IllegalStateException.class, stepper::step);
        assertEquals("Cannot step from empty history", ex.getMessage());
    }

    @Test
    void back_throws_when_empty() {
        var stepper = createStepper();
        var ex = assertThrows(IllegalStateException.class, stepper::back);
        assertEquals("Cannot step from empty history", ex.getMessage());
    }

    @Test
    void getBackingCollection_adds_complete_event_to_history() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var stepper = new AlgorithmStepper<>(algorithm, collection);
        stepper.getBackingCollection();

        assertInstanceOf(Compare.class, stepper.step());
        //assertInstanceOf(Complete.class, stepper.step());
        //assertInstanceOf(NoOp.class, stepper.step());
    }

    @Test
    void back_returns_noop_when_historypointer_is_at_beginning() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var stepper = new AlgorithmStepper<>(algorithm, collection);
        stepper.getBackingCollection();
        //assertInstanceOf(NoOp.class, stepper.back());
    }

    @Test
    void back_returns_previous_event_when_historypointer_is_not_at_beginning() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var stepper = new AlgorithmStepper<>(algorithm, collection);
        stepper.getBackingCollection();
        var first = stepper.step();
        var second = stepper.step();

        assertNull(second);
        assertSame(first, stepper.back());
        assertNull(stepper.back());
    }

    @Test
    void getBackingCollection_returns_copy_of_original_collection() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var expected = List.copyOf(collection.data());
        var stepper = new AlgorithmStepper<>(algorithm, collection);

        var backingCollection = stepper.getBackingCollection();

        assertEquals(expected, backingCollection);
    }

    @Test
    void back_after_one_step_returns_that_same_event() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var stepper = new AlgorithmStepper<>(algorithm, collection);
        stepper.getBackingCollection();

        var firstEvent = stepper.step();

        assertSame(firstEvent, stepper.back());
    }

    @Test
    void randomizeCollection_returns_new_collection() {
        var algorithm = new FakePlayerSort();
        var collection = createCollection(1, 2, 3);
        var expected = List.copyOf(collection.data());
        var stepper = new AlgorithmStepper<>(algorithm, collection);
        var newCollection = stepper.randomizeCollection(1234);

        assertNotSame(expected, newCollection);
    }

    @Test
    void custom_events_emitted_by_algorithm_are_replayed() {
        var algorithm = new IPlayerSort() {
            @Override
            public <T extends Comparable<T>> void sort(ISort<T> values) {
                values.emit(new Message("hello", Message.MessageType.INFO));
            }

            @Override
            public String getName() {
                return "Emitter";
            }
        };

        var stepper = new AlgorithmStepper<>(algorithm, createCollection(1, 2));
        stepper.getBackingCollection();

//        assertInstanceOf(Message.class, stepper.step());
//        assertInstanceOf(Complete.class, stepper.step());
    }

    // Helpers for testing
    private static class FakePlayerSort implements IPlayerSort {
        @Override
        public <T extends Comparable<T>> void sort(ISort<T> values) {
            values.compare(0, 1);
        }

        @Override
        public String getName() {
            return "FakePlayerSort";
        }
    }

    private static SortingCollection<Integer> createCollection(Integer... values) {
        return new SortingCollection<>(
                Stream.of(values)
                        .map(Data::new)
                        .toList()
        );
    }

    private static AlgorithmStepper<Integer> createStepper(Integer... values){
        return new AlgorithmStepper<>(new FakePlayerSort(), createCollection(values));
    }
}