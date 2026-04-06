//package io.github.mcalgovisualizations.visualization.renderer.dispatch;
//
//
//class DispatcherTest {
//
////    @Test
////    void dispatch_throws_IllegalStateException_when_no_registered_handlers() {
////        var event = new NoOp();
////        var ex = assertThrows(IllegalStateException.class, () -> new Dispatcher().dispatch(event));
////        assertEquals("No handler registered for event type " + event.getClass().getName(), ex.getMessage());
////    }
////
////    @Test
////    void dispatch_invokes_registered_handler() {
////        var dispatcher = new Dispatcher();
////        var event = new NoOp();
////        var expected = AnimationPlan.empty();
////
////        dispatcher.register(NoOp.class, e -> expected);
////
////        var result = dispatcher.dispatch(event);
////
////        assertSame(expected, result);
////    }
//}