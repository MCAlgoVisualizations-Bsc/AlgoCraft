# Architecture and Engine

AlgoCraft is organized as a multi-module Gradle project:

- lib: Shared engine and API logic for algorithm visualizations.
- prefab: Runnable Minestom-based server layer that consumes lib.

## Runtime Flow

1. The server starts from the prefab module entrypoint.
2. Visualization components from lib are initialized.
3. Player events trigger algorithm scene updates.
4. Rendering and lifecycle hooks run through Minestom event pipelines.

## Design Goals

- Keep visualization logic reusable and testable in lib.
- Isolate deployment/runtime specifics to prefab.
- Publish JavaDoc as the authoritative API reference.
