# Layouts and Displays

Layouts decide where values are placed. Displays decide how each value is represented in the world.

This split is important:

- Layout = spatial planning.
- Display = visual implementation.

When you keep these separate, you can change visuals without changing positioning logic, and vice versa.

## Layouts

Layouts implement ILayout<I> and return LayoutResult[] from compute(model, origin, instance).

Each LayoutResult binds three things:

- value: source model value.
- pos: computed world position.
- displayValue: render object implementing IDisplayValue.

Minimal rules:

- Always return a non-null array.
- Use origin as the anchor.
- Do not spawn entities directly in compute.

Why no direct spawning in compute:

The renderer controls initialization order and chunk readiness before scene.setLayout(...). Spawning early in layout can create race conditions and duplicate entities.

This project includes FloatingLinearLayout in prefab, which positions values along +X and uses MobDisplay per slot.

```java
public record FloatingLinearLayout<T>(double spacing, double yOffset, double zOffset)
        implements ILayout<List<T>> {

    @Override
    public LayoutResult[] compute(List<T> model, Pos origin, Instance instance) {
        LayoutResult[] out = new LayoutResult[model.size()];
        for (int i = 0; i < model.size(); i++) {
            Pos pos = new Pos(origin.x() + (i * spacing), origin.y() + yOffset, origin.z() + zOffset);
            out[i] = new LayoutResult(model.get(i), pos, new MobDisplay(pos, model.get(i).toString()));
        }
        return out;
    }
}
```

    When to create a new layout:

    - You need grid or graph positioning.
    - You need domain-specific spacing logic.
    - You need bounds-aware placement relative to origin.

## Displays

Displays implement IDisplayValue. They are scene-managed render objects that can be teleported, highlighted, removed, and optionally path-moved.

    A display can wrap one entity or multiple entities.
    Example: MobDisplay wraps both a creature and a text display and exposes them as one IDisplayValue.

Key methods:

- setInstance(instance) / setInstance(instance, pos)
- teleport(pos)
- setGlowing(highlighted)
- remove()
- isSpawned()

In prefab, MobDisplay is a concrete example that renders an entity and text display together.

When to create a custom display:

- You want custom visuals (blocks, particles, holograms, mobs).
- You need special highlight behavior.
- You need custom async movement via walkTo(...).

## Design guidance

- Keep value-to-position mapping in layout classes.
- Keep entity composition and visuals in display classes.
- Keep choreography in handlers.
- Keep orchestration and registration in bootstrap code.

## Common pitfalls

- Returning null from compute(...).
- Using slot index as world coordinate directly without origin offsets.
- Embedding animation timing in layout/display classes instead of handlers.
