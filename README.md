# Algorithm Visualization Bsc Minecraft Server (Minestom)

This is a custom Minecraft server built using the **Minestom** framework. It's purpose is to display various visualizations of different algorithms. It runs on a multi-container setup via Docker and DigitalOcean.

---

## 🎮 How to Join

To join, open Minecraft Java Edition (Version 1.21 or newer) and use **Direct Connect** with the addresses below.

### 🚀 Production Server (Official)
The stable version of the project.
- **Address:** `lukv.dev`


### 🛠️ Development Server (Beta)
Where we test the latest features. Expect bugs!
- **Address:** `lukv.dev:25566`

---

## 🛠️ Technical Info
- **Engine:** Minestom (Non-Mojang rewrite)
- **Language:** Kotlin / Java
- **OS:** CachyOS (Local) / Ubuntu (Server)
- **Infrastructure:** Docker Containers on DigitalOcean

---

## 🚀 Local Development
1. Clone the repository.
2. Run `./gradlew shadowJar` to build the "fat" jar.
3. Run `java -jar build/libs/*-all.jar` to start the server locally.
4. Connect via `localhost:25565`.

---

## 🤖 Updated Coding-Agent Prompt (based on `new_maze`)

```markdown
Please continue the 3D cave/tunnel pathfinding work on top of the existing `new_maze` branch changes.

Important: `new_maze` already includes a baseline villager flow:
- `VillagerMove` event and `VillagerMoveHandler`
- villager spawning/movement in `GridScene`
- camera follow (Villager POV) wiring via `ICameraTargetScene`, `AlgoCraft`, and UI toggle

Do **not** rebuild those from scratch. Reuse and extend them.

## Goal
Implement a new 3D cave/tunnel visualization with 3D pathfinding, while preserving the current locality/globality learning mode:
1. users can follow the villager locally (POV/spectate),
2. users can also fly out and view the global algorithm state.

## Requirements
1. **3D Cave Layout**
   - Add a new cave/tunnel layout generator (no external worldgen mods).
   - Generate traversable 3D tunnels from solid blocks.
   - Keep data model aligned with algorithm needs (graph/node mapping from carved spaces).

2. **3D Pathfinding Algorithm**
   - Add/adapt a 3D pathfinding implementation (BFS/DFS/A* style is fine).
   - Emit existing/new traversal events from algorithm logic only.
   - Keep Minestom rendering/entity code out of algorithm classes.

3. **Villager Visualization (extend existing system)**
   - Reuse the current villager event/handler pipeline from `new_maze`.
   - Move the villager through 3D tunnel nodes as exploration advances.
   - Keep explored vs exploring states visually distinct on tunnel floor/path blocks.

4. **POV + Global View**
   - Keep compatibility with current Villager POV toggle behavior.
   - Ensure POV continues to work with the new 3D cave scene camera target.
   - Do not remove free-fly/global observation capability.

5. **Scene/Registration**
   - Register the 3D cave algorithm in `Main.java` with proper presentation metadata.
   - Keep naming and wiring consistent with existing `Algorithm.build(...)` patterns.

## Architecture constraints
- Follow event-driven design used in this codebase.
- Keep algorithm, layout generation, scene rendering, and handlers clearly separated.
- Make focused, minimal changes; avoid unrelated refactors.
```
