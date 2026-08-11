package io.github.mcalgovisualizations.visualization.algorithm;

import io.github.mcalgovisualizations.visualization.utils.TestEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmStepperTest {

    @Test
    void constructor_throws_null_pointer_exception_for_null_history() {
        assertThrows(NullPointerException.class, () -> new AlgorithmStepper(null));
    }

    @Test
    void constructor_copies_list_defensively() {
        var mutableList = new ArrayList<IAlgorithmEvent>();
        mutableList.add(new TestEvent(1));

        var stepper = new AlgorithmStepper(mutableList);
        mutableList.clear();

        assertEquals(1, stepper.getHistorySize());
        assertFalse(stepper.isComplete());
    }

    @Test
    void empty_history_initial_state_and_navigation() {
        var stepper = new AlgorithmStepper(List.of());

        assertEquals(0, stepper.getHistorySize());
        assertTrue(stepper.isAtBeginning());
        assertTrue(stepper.isComplete());
        assertNull(stepper.step());
        assertNull(stepper.back());
    }

    @Test
    void step_advances_pointer_and_returns_events_in_order() {
        var event1 = new TestEvent(10);
        var event2 = new TestEvent(20);
        var stepper = new AlgorithmStepper(List.of(event1, event2));

        assertTrue(stepper.isAtBeginning());
        assertFalse(stepper.isComplete());

        assertEquals(event1, stepper.step());
        assertFalse(stepper.isAtBeginning());
        assertFalse(stepper.isComplete());

        assertEquals(event2, stepper.step());
        assertFalse(stepper.isAtBeginning());
        assertTrue(stepper.isComplete());

        assertNull(stepper.step());
    }

    @Test
    void back_moves_pointer_backwards_and_returns_previous_event() {
        var event1 = new TestEvent(1);
        var event2 = new TestEvent(2);
        var stepper = new AlgorithmStepper(List.of(event1, event2));

        assertNull(stepper.back());

        stepper.step();
        stepper.step();

        assertEquals(event2, stepper.back());
        assertEquals(event1, stepper.back());
        assertTrue(stepper.isAtBeginning());
        assertNull(stepper.back());
    }

    @Test
    void reset_resets_pointer_to_start() {
        var event1 = new TestEvent(5);
        var stepper = new AlgorithmStepper(List.of(event1));

        stepper.step();
        assertTrue(stepper.isComplete());

        stepper.reset();
        assertTrue(stepper.isAtBeginning());
        assertFalse(stepper.isComplete());
        assertEquals(event1, stepper.step());
    }
}