# AGENTS.md - Algorithm Visualization Minecraft Server

## Project Overview

This is a **Minestom-based Minecraft server** that visualizes sorting algorithms in real-time within the game world. Players see data elements as blocks, watch comparisons/swaps animate in 3D space, and interact with the visualization via inventory items.

**Key Tech**: Java/Kotlin, Minestom (1.21.11), Gradle multi-module (lib + server), Docker containerized, JUnit 5 tests with JaCoCo coverage.

---

## Architecture Essentials

### Three-Layer Stack

1. **`lib/` (Reusable Visualization Engine)**
   - Pure Java algorithm visualization framework (no Minestom dependency)
   - `AlgoCraft`: Main orchestrator registering algorithms + UI
   - `VisualizationController`: Per-player state machine (play/pause/step/randomize)
   - `AlgorithmStepper<T>`: Event-based history system capturing algorithm steps
   - Compiles to JAR consumed by server module

2. **`server/` (Minestom Game Server)**
   - Entry point: `Main.java` (hardcoded algorithm instances + placements)
   - Registers algorithms via `AlgoCraft.registerAlgorithm()` with placement coordinates
   - Uses ShadowJar to build fat JAR: `minecraft-server-all.jar`
   - Listens on port 25565 (prod) / 25566 (dev) via Docker
   - Commands: `/spawn`, `/greet`, `/teleport`, `/gamemode` (under `commands/` package)

3. **Visualization Pipeline**
   - **Models** (`lib/src/main/java/.../models/`): `Data<T>`, `SortingCollection`, `ISort` interface
   - **Layouts** (`layouts/`): Position calculations (`FloatingLinearLayout`, `CircleLayout`, `MatrixLayout`)
   - **Renderer** (`renderer/`): Converts algorithm events → Minestom display entities (holograms)
   - **Engine** (`engine/VisualizationController`): Ticks algorithm forward/backward, publishes events

### Event-Driven Algorithm Execution

```
Algorithm.step() → AlgorithmStepper captures event (Compare/Swap/Complete)
  → VisualizationController broadcasts to Renderer
  → Renderer animates changes as block/hologram movements in world
```

**Critical**: `AlgorithmStepper` records ALL steps, enabling perfect playback backward via `back()`.

---

## Build & Deployment

### Local Development
```bash
./gradlew shadowJar          # Builds server/build/libs/minecraft-server-all.jar
java -jar build/libs/*-all.jar  # Runs on localhost:25565
```

### Docker Multi-Stage Build
- **Builder**: Compiles with `./gradlew shadowJar` inside container
- **Runtime**: Lightweight JRE (Java 25) with 1-2GB heap limits
- **Profiles**: `--profile prod` (port 25565) or `--profile dev` (port 25566)
- Volumes: `prod-data:/app/world`, `dev-data:/app/world` persist world state

### Key Gradle Config
- **Toolchain**: Java 25 (enforced across both modules)
- **Dependencies**: Minestom `2026.01.08-1.21.11`, Guava, Commons Math3, SLF4J/Logback
- **Testing**: JUnit 5 (Jupiter) + JaCoCo coverage reports
- **Fat JAR**: ShadowJar plugin with Main-Class manifest

---

## Critical Patterns

### Algorithm Registration (Main.java)
```java
algo.registerAlgorithm(
    "insertion sort (ints)",
    PlayerInsertion::new,         // Supplier of IPlayerSort
    integerCollection1,           // SortingCollection<Data<Integer>>
    new FloatingLinearLayout(),   // Layout strategy
    new AlgorithmPlacement(renderOrigin, teleportPoint)
);
```

### Adding New Algorithms
1. Implement `IPlayerSort` interface in `server/src/main/java/.../algorithms/`
2. Override `step(AlgorithmStepper stepper)` to drive stepping logic
3. Call `stepper.compareIndex(i, j)`, `stepper.swap(i, j)` to record events
4. Register in `Main.java` with data + placement

### Event Dispatch System
- `InventoryPreClickEvent`: Players click UI items in hotbar
- `PlayerUseItemEvent`: Item right-click triggers algorithm action (start/pause/randomize/step)
- UI items tagged with `ALGO_SELECTOR_TAG`, `ALGO_INTERACTION_TAG` (via `Tags` constants)

### Testing Approach
- `AlgorithmStepperTest`: Validates event history (forward/backward stepping)
- Tests use `FakePlayerSort` (mock) to avoid Minestom dependency in lib
- Renderer tests separate (in `lib/src/test/.../renderer/`)

---

## Code Organization

```
lib/src/main/java/.../visualization/
├── AlgoCraft.java              # Registry + event listeners
├── algorithms/                 # IPlayerSort implementations
├── engine/VisualizationController  # Per-player state machine
├── layouts/                    # Position calculation strategies
├── models/                     # Data<T>, SortingCollection, ISort
├── renderer/                   # Executor + Scene + Display
├── gui/                        # Button/inventory logic
├── ui/                         # AlgorithmUI, AlgorithmPresentation
└── items/                      # Custom Minecraft items

server/src/main/java/.../
├── Main.java                   # Server bootstrap
├── algorithms/PlayerInsertion, ThanosSort  # Concrete algorithm impls
├── commands/                   # /spawn, /greet, etc.
└── config/WorldConfig          # Instance creation
```

---

## Deployment Specifics

- **Production**: `lukv.dev` (port 25565 via DigitalOcean)
- **Development**: `lukv.dev:25566` (beta/testing)
- **World Data**: Persistent across restarts (Docker volumes)
- **Server Time**: Fixed at noon (time rate = 0) for consistent visuals
- **Spawn Point**: `(194, 137, -38)` in Main.java (HUB_SPAWN constant)

---

## Common Tasks

| Task | Command |
|------|---------|
| Build locally | `./gradlew shadowJar` |
| Run tests | `./gradlew test` |
| Coverage report | `./gradlew jacocoTestReport` (HTML in `build/reports/jacoco/`) |
| Start server | `java -jar server/build/libs/minecraft-server-all.jar` |
| Rebuild in Docker | `docker compose --profile dev up --build` |
| Add algorithm | Implement `IPlayerSort`, register in `Main.java` |

---

## Pitfalls & Conventions

- **Don't**: Add direct Minestom imports to `lib/` code (breaks reusability). Use `compileOnly` in gradle.
- **Do**: Use `SortingCollection<Data<T>>` wrapper for generic algorithm data.
- **Data comparability**: `Data<T>` requires `T extends Comparable<T>`.
- **Placement coordinates**: Both `renderOrigin` and `teleportPoint` must be set in `AlgorithmPlacement`.
- **Events as history**: Always use `AlgorithmStepper.compareIndex()` and `AlgorithmStepper.swap()` to ensure UI stays in sync.

