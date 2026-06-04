# Events and Handlers

AlgoCraft uses an event-driven rendering pipeline:

1. Algorithm emits IAlgorithmEvent values.
2. Dispatcher selects a matching IAnimationHandler.
3. Handler returns an AnimationPlan.
4. Renderer executes the plan on the active scene.

This design decouples algorithm correctness from visual style. The algorithm only emits facts. Handlers decide presentation.

## Pipeline responsibilities

- Algorithm: decides what happened.
- Event: immutable payload of that decision.
- Handler: maps that payload to animation steps.
- Scene: executes steps against displays.

## Events

Events should be immutable data records. Prefab examples:

- Compare
- Swap
- Message

```java
public record Compare(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {}
```

Event modeling tips:

- Use small, explicit events over one generic mega-event.
- Include indices and values if handlers need both movement and messaging.
- Keep events deterministic and serializable in intent.

## Handlers

Handlers convert one event into one animation plan.

```java
public final class CompareHandler implements IAnimationHandler<Compare> {
    @Override
    public AnimationPlan<ISceneOps> handle(Compare event) {
        return AnimationPlan.builder()
                .step(1, s -> s.setHighlighted(event.x(), true))
                .step(1, s -> s.setHighlighted(event.y(), true))
                .step(1, s -> s.hoverDisplay(event.x(), true))
                .step(1, s -> s.hoverDisplay(event.y(), true))
                .step(1, s -> s.hoverDisplay(event.x(), false))
                .step(1, s -> s.hoverDisplay(event.y(), false))
                .build();
    }
}
```

    AnimationPlan semantics:

    - step(ticks, op): enqueue a synchronous scene operation after delay ticks.
    - stepAsync(ticks, op): enqueue asynchronous movement/effect and await completion.
    - instant(...): shorthand for one-step plans.

## Registration pattern

Register all handlers during Algorithm.builder(...):

```java
.onEvent(Compare.class, new CompareHandler())
.onEvent(Swap.class, new SwapHandler())
.onEvent(Message.class, new MessageHandler())
```

If you emit an event without a registered handler, dispatch fails at runtime.

Dispatch uses concrete event class matching, not inheritance fallthrough.

## Completion behavior

Use onCompletion(...) in Algorithm.builder(...) for end-of-run messaging or finale animations.

If omitted, the builder provides a default completion message plan.

## AnimationPlan tips

- Use step(ticks, op) for timed sync operations.
- Use stepAsync when the scene operation returns CompletableFuture.
- Keep each event plan small and composable.
- Put end-of-run behavior in onCompletion(...).

## Common pitfalls

- Adding heavy algorithm logic inside handlers.
- Registering handler for wrong event class.
- Emitting events in unstable order, causing jittery or confusing playback.
