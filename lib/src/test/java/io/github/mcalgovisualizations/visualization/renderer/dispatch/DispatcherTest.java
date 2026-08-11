package io.github.mcalgovisualizations.visualization.renderer.dispatch;

import io.github.mcalgovisualizations.visualization.algorithm.IAlgorithmEvent;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.utils.TestEvent;
import io.github.mcalgovisualizations.visualization.utils.TestEventHandler;
import io.github.mcalgovisualizations.visualization.utils.TestSceneOps;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    private record UnregisteredEvent() implements IAlgorithmEvent {}

    @Test
    void dispatch_executes_handler_for_registered_event() {
        var handler = new TestEventHandler();
        Map<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>> handlers = Map.of(
                TestEvent.class, handler
        );

        var dispatcher = new Dispatcher<TestSceneOps>(handlers);
        var plan = dispatcher.dispatch(new TestEvent(42));

        assertNotNull(plan);
        assertTrue(plan.isEmpty());
    }

    @Test
    void dispatch_throws_illegal_state_exception_when_no_handler_registered() {
        var dispatcher = new Dispatcher<TestSceneOps>(Map.of());
        var unregisteredEvent = new UnregisteredEvent();

        var exception = assertThrows(
                IllegalStateException.class,
                () -> dispatcher.dispatch(unregisteredEvent)
        );

        assertTrue(exception.getMessage().contains("No handler registered for event type"));
        assertTrue(exception.getMessage().contains(UnregisteredEvent.class.getName()));
    }

    @Test
    void dispatch_throws_null_pointer_exception_for_null_event() {
        var dispatcher = new Dispatcher<TestSceneOps>(Map.of());

        var exception = assertThrows(
                NullPointerException.class,
                () -> dispatcher.dispatch(null)
        );

        assertEquals("event", exception.getMessage());
    }

    @Test
    void constructor_copies_handlers_defensively() {
        var mutableMap = new HashMap<Class<? extends IAlgorithmEvent>, IAnimationHandler<?>>();
        mutableMap.put(TestEvent.class, new TestEventHandler());

        var dispatcher = new Dispatcher<TestSceneOps>(mutableMap);

        mutableMap.clear();

        assertNotNull(dispatcher.dispatch(new TestEvent(10)));
    }

    @Test
    void constructor_throws_null_pointer_exception_for_null_map() {
        assertThrows(NullPointerException.class, () -> new Dispatcher<TestSceneOps>(null));
    }
}