# UI

AlgoCraft has two layers of user interface:

1. The algorithm selector UI, which lets players choose what to run.
2. The running UI, which gives the active player controls while a visualization is playing.

Both are provided by the framework by default through AlgorithmUI.

## Selector UI

When a player uses the selector item in the hub, AlgoCraft opens an inventory listing every registered algorithm.

What appears in the selector comes from AlgorithmPresentation:

- Algorithm id
- Icon
- Complexity text
- Short description

If you do not provide a presentation, the framework falls back to a default stick icon and the algorithm id.

## Running UI

When a visualization starts, the player's hotbar is replaced with the running layout.

Default controls are:

- Randomize
- Start
- Stop
- Step Forward
- Step Back
- Change Speed
- Clear Algorithm

If the algorithm supports POV mode, a Villager POV item is also added.

## Default behavior

The default running UI is already wired in the algorithm builder:

```java
.withRunningLayout(new AlgorithmUI())
```

So in most cases, UI is on by default. You only need to override it if you want a custom selector or different hotbar controls.

## Customization

Use a custom implementation of `IAlgorithmUI` when you want to change:

- how the selector inventory is laid out
- what items appear during playback
- the text or icons shown to the player

The framework will still handle the actual playback logic. The UI only changes how players interact with the session.

## Related API

- AlgorithmPresentation
- IAlgorithmUI
- AlgorithmUI