# Scenes

A scene is the in-world execution surface for your visualization. Handlers do not mutate entities directly. They call scene operations.

In practice, a scene is the boundary between abstract algorithm events and concrete Minestom entities.

## Why scenes exist

Without scenes, handlers would need to know too much about entities, slots, teleport logic, and cleanup.
Scenes centralize those concerns and expose stable operations through ISceneOps.

## Scene contract

Scene implementations expose ISceneOps operations, including:

- setLayout(LayoutResult[])
- setHighlighted(int slot, boolean highlighted)
- hoverDisplay(int slot, boolean hover)
- swapSlots(int a, int b)
- moveSlotTo(int slot, Pos position)
- sendMessage(...) and sendActionBar(...)
- cleanUp()

These operations are intentionally high-level. A handler asks for a swap/highlight/hover and does not care if the underlying display is a mob, block, text, or particle.

## SceneContext

Scene constructors receive SceneContext:

- instance: Minestom instance where visuals exist
- audience: message/sound channel for players
- origin: spatial anchor position

This keeps scenes testable and deterministic:

- instance controls where rendering exists.
- audience controls who receives feedback.
- origin keeps layouts and animations anchored consistently.

## Default scene usage

Most algorithms can start with DefaultScene:

```java
.withScene(DefaultScene::new)
```

This is enough if your handlers only need core slot operations.

Use DefaultScene when:

- You have one display per slot.
- You only need built-in slot operations.
- You do not need custom camera or scene-specific state.

## Custom scene and POV support

If you need camera logic or additional scene state:

1. Extend AbstractScene.
2. Override setLayout(LayoutResult[]).
3. Optionally implement VillagerPOV to support the POV toggle item.

```java
public final class MyScene extends AbstractScene implements VillagerPOV {
    public MyScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layout) {
        for (int i = 0; i < layout.length; i++) {
            addDisplay(i, layout[i].displayValue());
        }
    }

    @Override
    public Entity cameraTarget() {
        return null; // return active entity to ride when POV is enabled
    }
}
```

Implement a custom scene when:

- You have multiple display layers per slot.
- You need extra lookup tables or runtime scene metadata.
- You need VillagerPOV camera behavior.

For POV support specifically:

- Implement VillagerPOV.
- Return a valid active entity from cameraTarget().
- Optionally implement setLocatorBarVisible(...) and onPovToggle(...).

## Practical rule

Keep algorithm logic in algorithm classes, visual effects in handlers, and world-state/display concerns in scene classes.

## Common pitfalls

- Storing display state outside the scene and desynchronizing slot mappings.
- Forgetting cleanUp() semantics for custom display resources.
- Performing algorithm decisions inside scene code.
