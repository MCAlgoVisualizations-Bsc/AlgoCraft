# Making Your First Algorithm

This guide explains the algorithm side of AlgoCraft in a Java-first way.

Overview:

- Your algorithm mutates model data.
- While mutating data, it emits immutable events.
- The renderer replays those events through handlers.

Think of this as command sourcing for visual state: data changes and visual intentions are recorded together.

## Step 1: Define your context

Your context should implement AlgorithmContext. In this repo, the typical pattern is extending AbstractContext and implementing copy methods.

```java
import io.github.mcalgovisualizations.visualization.models.AbstractContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MyContext extends AbstractContext<List<Integer>> {
    public MyContext(List<Integer> values) {
        super(new ArrayList<>(values));
    }

    @Override
    public MyContext copy() {
        MyContext c = new MyContext(copyData());
        c.events.addAll(this.events);
        return c;
    }

    @Override
    public List<Integer> copyData() {
        return new ArrayList<>(values);
    }

    @Override
    public List<Integer> randomizeData() {
        Collections.shuffle(values);
        return copyData();
    }
}
```

    Why copy and randomize matter:

    - copy(): used to build independent traces safely.
    - copyData(): snapshot for initialization and deterministic replay.
    - randomizeData(): source for the Randomize control in runtime UI.

## Step 2: Define events your algorithm emits

Events are immutable records implementing IAlgorithmEvent.

```java
public record Compare(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {}
public record Swap(int x, int y, Object xValue, Object yValue) implements IAlgorithmEvent {}
```

Event design guidelines:

- Include slot indices when your handler targets scene slots.
- Include display text values for action-bar/message feedback.
- Keep events immutable and side-effect free.

## Step 3: Implement the algorithm

Implement IPlayerSort and emit events through context.emit(...).

```java
import io.github.mcalgovisualizations.visualization.algorithm.IPlayerSort;

public final class MyFirstSort implements IPlayerSort<MyContext> {
    @Override
    public void run(MyContext context) {
        var values = context.getData();
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < values.size() - 1 - i; j++) {
                context.emit(new Compare(j, j + 1, values.get(j), values.get(j + 1)));
                if (values.get(j) > values.get(j + 1)) {
                    int a = values.get(j);
                    int b = values.get(j + 1);
                    values.set(j, b);
                    values.set(j + 1, a);
                    context.emit(new Swap(j, j + 1, a, b));
                }
            }
        }
    }
}
```

    Important detail:

    The renderer does not inspect your algorithm internals. It only sees emitted events.
    If a visual action should exist, emit an event for it.

## Step 4: Register handlers for every event type

If an emitted event has no registered handler, rendering fails with a dispatch error.

```java
.onEvent(Compare.class, new CompareHandler())
.onEvent(Swap.class, new SwapHandler())
.onEvent(Message.class, new MessageHandler())
```

This is a strict one-to-one contract:

- Emit Compare -> register CompareHandler.
- Emit Swap -> register SwapHandler.
- Emit custom event -> register custom handler.

## Step 5: Register algorithm in your bootstrap

```java
algoCraft.registerAlgorithm(
    Algorithm.<List<Integer>, MyContext, ISceneOps>builder(new MyContext(List.of(5, 1, 4, 2, 3)))
        .withIdentity("My First Sort", MyFirstSort::new)
        .positioning(new FloatingLinearLayout<Integer>(2.0, 0.0, 0.0))
        .withScene(DefaultScene::new)
        .onEvent(Compare.class, new CompareHandler())
        .onEvent(Swap.class, new SwapHandler())
        .create()
);
```

    Execution lifecycle after registration:

    1. Player selects algorithm from selector.
    2. AlgoCraft creates AlgorithmInstance.
    3. Renderer initializes scene from layout output.
    4. Controller replays algorithm trace event-by-event.
    5. Handlers transform events into AnimationPlan steps.

## Checklist

- Context supports copy, copyData, randomizeData.
- Algorithm emits immutable IAlgorithmEvent records.
- Every emitted event has a matching onEvent registration.
- Layout returns non-null LayoutResult[] values.
- Scene supports operations your handlers call (swap, highlight, hover, messages).

    ## Common pitfalls

    - Mutating shared lists between runs instead of returning copies.
    - Emitting too little data in events (handler then has to guess).
    - Using event inheritance and expecting superclass handlers to match (dispatch is by concrete class).
