# AlgoCraft Framework

AlgoCraft is a Java framework for building interactive algorithm visualizations in Minecraft using Minestom.

This repository is primarily the framework codebase, with a runnable example application included.

## Repository Purpose

- Provide reusable visualization contracts and engine components.
- Provide event-driven rendering primitives for algorithm playback.
- Provide a prefab app module showing how to wire algorithms, events, handlers, layouts, and displays.

## Modules

- `lib`: Core framework and public API surface.
- `prefab`: Example Minestom application built on top of `lib`.

## Documentation

- WikiHub: https://mcalgovisualizations-bsc.github.io/AlgoCraft/
- JavaDoc: https://mcalgovisualizations-bsc.github.io/AlgoCraft/javadoc/

For a full demo world, see: https://github.com/MCAlgoVisualizations-Bsc/Demo

## Local Development

1. Build and run tests:
	- `./gradlew test`
2. Build the runnable prefab jar:
	- `./gradlew :prefab:shadowJar`
3. Run the example app:
	- `java -jar prefab/build/libs/minecraft-server-all.jar`

## Notes

- Minestom powers the runtime world/entity layer.
- Visualization flow is event-driven: algorithms emit events, handlers convert events to animation plans, and scenes execute those plans.
