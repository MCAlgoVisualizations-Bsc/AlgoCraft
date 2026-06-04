# Getting Started

This page walks through a minimal Minestom flat-world server setup and then connects it to AlgoCraft.

The goal is to make the full pipeline understandable:

1. Boot a Java server.
2. Create an AlgoCraft instance.
3. Register one algorithm.
4. Join in Minecraft and run it.

If you are new to the codebase, treat this as the shortest path from zero to first visualization.

## Prerequisites

- Java 25
- Gradle 8+
- Minecraft Java 1.21+

## Project structure (high level)

- lib: reusable visualization engine, contracts, rendering pipeline.
- prefab: runnable Minestom app that wires concrete algorithms, events, handlers, and displays.

In other words, lib defines what an algorithm visualization is, and prefab defines one concrete way to run and render it.

## 1) Start a minimal flat Minestom server

Create a Java entrypoint like this:

```java
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;

public final class Main {
   public static void main(String[] args) {
      MinecraftServer server = MinecraftServer.init();
      InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();

      instance.setGenerator(unit -> {
         for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
               unit.modifier().setBlock(x, 39, z, Block.GRASS_BLOCK);
            }
         }
      });

      MinecraftServer.getGlobalEventHandler().addListener(
            net.minestom.server.event.player.PlayerLoginEvent.class,
            event -> {
               event.setSpawningInstance(instance);
               event.getPlayer().setRespawnPoint(new Pos(0.5, 40, 0.5));
            }
      );

      server.start("0.0.0.0", 25565);
   }
}
```

   What this does:

   - Initializes Minestom.
   - Creates one instance.
   - Generates a flat grass platform.
   - Spawns players into that instance.
   - Starts listening on port 25565.

## 2) Add AlgoCraft library dependency

   If you are inside this repository, use the local module dependency in build.gradle.kts:

```kotlin
dependencies {
   implementation(project(":lib"))
}
```

   If you consume AlgoCraft externally, use your published coordinates and version tag:

```kotlin
repositories {
   mavenCentral()
   maven("https://jitpack.io")
}

dependencies {
   implementation("<your.group.or.jitpack>:<artifact>:<version>")
}
```

Tip: keep Minestom and AlgoCraft versions aligned with your server module to avoid API drift.

## 3) Wire AlgoCraft into your server bootstrap

AlgoCraft needs a default instance and listeners:

```java
import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;

public final class Main {
   public static void main(String[] args) {
      MinecraftServer server = MinecraftServer.init();
      InstanceContainer hub = MinecraftServer.getInstanceManager().createInstanceContainer();

      AlgoCraft algoCraft = new AlgoCraft(hub);
      algoCraft.addListener(MinecraftServer.getGlobalEventHandler());

      // register algorithms here (see next section)

      server.start("0.0.0.0", 25565);
   }
}
```

   Conceptually:

   - AlgoCraft is your runtime registry plus session manager.
   - addListener(...) wires item interactions and selector behavior.
   - registerAlgorithm(...) provides the playable entries in the selector UI.

## 4) Register a first simple visualization

Use Algorithm.builder(model) from lib and register it on AlgoCraft:

```java
algoCraft.registerAlgorithm(
   Algorithm.<Integer, MyContext, ISceneOps>builder(new MyContext(List.of(4, 2, 1, 3)))
      .withIdentity("My First Sort", MyFirstSort::new)
      .positioning(new FloatingLinearLayout<Integer>(2.0, 0.0, 0.0))
      .withScene(DefaultScene::new)
      .onEvent(Compare.class, new CompareHandler())
      .onEvent(Swap.class, new SwapHandler())
      .onEvent(Message.class, new MessageHandler())
      .withPresentation(new AlgorithmPresentation(
         "My First Sort",
         net.minestom.server.item.Material.BOOK,
         "O(n^2)",
         "A tiny demo algorithm to verify the full rendering pipeline"
      ))
      .create()
);
```

   What each builder section means:

   - withIdentity: algorithm id + constructor for the runnable algorithm.
   - positioning: value-to-position transformation.
   - withScene: which scene implementation handles slot operations.
   - onEvent: event type to animation handler mapping.
   - withPresentation: selector icon, title, complexity, description.

For a full breakdown of each piece, continue with:

- Making Your First Algorithm
- Scenes
- Layouts and Displays
- Events and Handlers

## 5) Build and run this repository locally

```bash
./gradlew test
./gradlew :prefab:shadowJar
java -jar prefab/build/libs/minecraft-server-all.jar
```

Then join from Minecraft with localhost:25565.

Open the selector item, choose your algorithm, and use the running-layout controls to start/step/randomize.

## 6) Build docs locally

```bash
./gradlew :lib:javadoc
mkdocs build
```

## Common setup issues

- No algorithm appears in selector:
   algorithm was not registered, or registration failed during bootstrap.
- Runtime dispatch error:
   at least one emitted event type is missing an onEvent handler.
- Empty scene:
   layout returned empty results or displays were not assigned in setLayout(...).
