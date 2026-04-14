package io.github.mcalgovisualizations.visualization;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.mcalgovisualizations.visualization.ui.InteractionType.SPAWN;
import static io.github.mcalgovisualizations.visualization.ui.Tags.*;

public class AlgoCraftNew {
    private final Map<String, Algorithm<?,?,?>> algorithmRegistry = new HashMap<>();
    // player uuid for fast lookup
    private final Map<UUID, AlgorithmInstance<?,?,?>> playerInstance = new HashMap<>();
    private final Map<UUID, PlayerInventory> playerInventory = new HashMap<>();
    private final AlgorithmUI ui = new AlgorithmUI();
    private final Instance defaultInstance;

    public AlgoCraftNew(InstanceContainer defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    public AlgorithmInstance<?,?,?> createInstance(String id, Player... players){
        if (algorithmRegistry.get(id) == null)
            throw new IllegalArgumentException("No entry with id " + id);
        var algorithm = algorithmRegistry.get(id);
        return new AlgorithmInstance<>(algorithm, players);
    }

    public void addPlayerToInstance(UUID instanceId, Player... players) {
        final var instance = playerInstance.get(instanceId);
        if (instance == null) {
            createInstance(instanceId, players);
        }
        instance.addPlayer(players);
    }

    public void removePlayerFromInstance(Player... players) {
        for(var player : players) {
            var instance = requireInstance(player);
            instance.removePlayer(defaultInstance, player);
            playerInstance.remove(player.getUuid());
        }

        // dereference and clear instance if members are empty
        playerInstance.entrySet().removeIf(entry -> {
            var algorithmInstance = entry.getValue();
            if (algorithmInstance.isEmpty()) {
                algorithmInstance.getController().clear();
                return true;
            }
            return false;
        });
    }

    public <T, C extends AlgorithmContext<T>> void registerAlgorithm(Algorithm<T, C, ?> algorithm) {
        algorithmRegistry.put(algorithm.id(), algorithm);
    }

    private AlgorithmInstance<?,?,?> requireInstance(Player player) {
        var instance = playerInstance.get(player.getUuid());
        if (instance == null) {
            throw new IllegalStateException("No instance for player " + player.getUsername());
        }
        playerInventory.put(player.getUuid(), player.getInventory());
        return instance;
    }

    public void addListener(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, this::onPlayerUseItem);
        handler.addListener(InventoryPreClickEvent.class, this::onInventoryPreClick);
        handler.addListener(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    private void onPlayerUseItem(PlayerUseItemEvent event) {
        final var player = event.getPlayer();
        final var instance = requireInstance(player);
        final ItemStack itemStack = event.getItemStack();

        if (!itemStack.hasTag(ALGO_SELECTOR_TAG) && !itemStack.hasTag(ALGO_INTERACTION_TAG)) {
            return;
        }

        event.setCancelled(true);

        if (itemStack.hasTag(ALGO_SELECTOR_TAG)) {
            selectAlgorithm(player);
            return;
        }

        if (itemStack.getTag(ALGO_INTERACTION_TAG).equals(SPAWN)) {
            // player.setInstance(defaultInstance);
            // should
            return;
        }

        if (session == null) {
            System.err.println("No controls for player " + player.getUsername());
            return;
        }

        switch (itemStack.getTag(ALGO_INTERACTION_TAG)) {
            case RANDOMIZE -> instance.getController().randomize();
            case START -> instance.getController().start();
            case STOP -> instance.getController().pause();
            case FORWARD -> instance.getController().step();
            case BACKWARD -> instance.getController().back();
            case SET_SPEED -> instance.getController().changeSpeed();
            default -> {
                ui.applyDefaultLayout(player);
            }
        }
    }

    private void onInventoryPreClick(InventoryPreClickEvent event) {
        final var player = event.getPlayer();
        final var inventory = event.getInventory();
        if (event.getPlayer() != player) return;
        if (event.getInventory() != inventory) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getClickedItem();
        if (clickedItem.isAir()) return;

        final var algorithmId = clickedItem.getTag(ALGO_ID_TAG);
        if (algorithmId == null) return;

        var originalPos = player.getPosition();

        player.closeInventory();
        ui.applyRunningLayout(player);

        MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    try {
                        final var instance = createInstance(algorithmId, player);
                        instance.teleportPlayer(player);
                    } catch (IllegalStateException e) {
                        player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                    } catch (NullPointerException e) {
                        player.sendMessage(Component.text("Failed to assign visualization : ", NamedTextColor.RED));
                        player.teleport(originalPos);
                        ui.applyDefaultLayout(player);
                    }
                })
                .delay(Duration.ofMillis(200))
                .schedule();
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        removePlayerFromInstance(event.getPlayer());
    }
}
