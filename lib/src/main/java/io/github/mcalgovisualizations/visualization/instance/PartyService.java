package io.github.mcalgovisualizations.visualization.instance;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PartyService {
    private final Player host;
    private final Set<Player> spectators = new HashSet<>();

    public PartyService(Player host) {
        this.host = host;
    }

    /**
     * Adds the given players to the given instance at the given position.
     * @param instance new instance
     * @param pos new position
     * @param players players to add
     * @return a future that completes when all players have been added
     */
    public CompletableFuture<Void> addSpectator(Instance instance, Pos pos, Player... players) {
        CompletableFuture<?>[] futures = Arrays.stream(players)
            .map(player -> {
                if (spectators.contains(player)) {
                    player.sendMessage(Component.text(
                        "You are already spectating this session",
                        NamedTextColor.RED
                    ));
                    return CompletableFuture.completedFuture(null);
                }

                return player.setInstance(instance, pos)
                    .thenRun(() -> {
                        if(player.getUuid() == host.getUuid())
                            player.setGameMode(GameMode.ADVENTURE);
                        else
                            player.setGameMode(GameMode.SPECTATOR);
                        spectators.add(player);

                        player.setAllowFlying(true);

                        audience().sendMessage(Component.text(
                            player.getUsername() + " joined the session",
                            NamedTextColor.GREEN
                        ));
                    });
            })
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * Removes the given players from the given instance at the given position.
     * @param instance return instance
     * @param players players to remove
     * @return a future that completes when all players have been removed
     */
    public CompletableFuture<Void> removeSpectator(Instance instance, Pos origin, Player... players) {
        CompletableFuture<?>[] futures = Arrays.stream(players)
            .map(player -> {
                if (player.getUuid().equals(host.getUuid())) {
                    return disband(instance, origin);
                }

                if (!spectators.contains(player)) {
                    player.sendMessage(Component.text(
                        "You are not spectating this session",
                        NamedTextColor.RED
                    ));
                    return CompletableFuture.completedFuture(null);
                }

                return player.setInstance(instance, origin)
                    .thenRun(() -> {
                        spectators.remove(player);
                        player.setGameMode(GameMode.ADVENTURE);

                        audience().sendMessage(Component.text(
                            player.getUsername() + " left the session",
                            NamedTextColor.YELLOW
                        ));
                    });
            })
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    /**
     * Disbands the party by moving all players to the given instance at the given position.
     * @param instance new instance
     * @param pos new position
     * @return a future that completes when all players have been moved
     */
    private CompletableFuture<Void> disband(Instance instance, Pos pos) {
        var playersToMove = new HashSet<>(spectators);
        playersToMove.add(host);

        CompletableFuture<?>[] futures = playersToMove.stream()
            .map(player -> player.setInstance(instance, pos)
            .thenRun(() -> player.setGameMode(GameMode.ADVENTURE)))
            .toArray(CompletableFuture[]::new);

        spectators.clear();
        return CompletableFuture.allOf(futures);
    }

    /**
     * Returns an audience that includes all players in the party.
     * @return an audience that includes all players in the party
     */
    public Audience audience() {
        var players = new HashSet<>(spectators);
        players.add(host);
        return Audience.audience(players);
    }


}