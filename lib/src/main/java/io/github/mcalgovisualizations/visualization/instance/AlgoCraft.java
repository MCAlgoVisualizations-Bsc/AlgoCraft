package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.commands.Accept;
import io.github.mcalgovisualizations.visualization.commands.Invite;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
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

public class AlgoCraft {
    private final Map<String, Algorithm<?,?,?>> algorithmRegistry = new HashMap<>();
    // player uuid for fast lookup
    private final Map<UUID, AlgorithmInstance<?,?,?>> playerInstance = new HashMap<>();
    private final Map<UUID, PlayerInventory> playerInventory = new HashMap<>();
    private final Map<String, AlgorithmPresentation> presentations = new HashMap<>();
    private final AlgorithmUI ui = new AlgorithmUI();
    private final Instance defaultInstance;

    public AlgoCraft(InstanceContainer defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    public AlgorithmInstance<?,?,?> createInstance(String id, Player... players){
        if (algorithmRegistry.get(id) == null)
            throw new IllegalArgumentException("No entry with id " + id);
        var algorithm = algorithmRegistry.get(id);
        var instance = new AlgorithmInstance<>(algorithm, players);
        Arrays.stream(players).forEach(player -> {
            playerInventory.put(player.getUuid(), player.getInventory());
            playerInstance.put(player.getUuid(), instance);
        });
        return instance;
    }

    public void addPlayerToInstance(UUID instanceId, Player... players) {
        final var instance = playerInstance.get(instanceId);
        if (instance == null) {
            //createInstance(instanceId, players);
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

    public AlgorithmInstance<?,?,?> requireInstance(Player player) {
        var instance = playerInstance.get(player.getUuid());
        if (instance == null) {
            throw new IllegalStateException("No instance for player " + player.getUsername());
        }
        return instance;
    }

    public void applyDefaultLayout(Player player) {
        ui.applyDefaultLayout(player);
    }

    public void addListener(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, this::onPlayerUseItem);
        MinecraftServer.getCommandManager().register(new Invite(this));
        MinecraftServer.getCommandManager().register(new Accept(this));
//        handler.addListener(InventoryPreClickEvent.class, this::onInventoryPreClick);
//        handler.addListener(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    private void onPlayerUseItem(PlayerUseItemEvent event) {
        event.setCancelled(true);
        final var player = event.getPlayer();
        final var instance = playerInstance.get(player.getUuid());
        final ItemStack itemStack = event.getItemStack();
        if (itemStack.hasTag(ALGO_SELECTOR_TAG)) {
            selectAlgorithm(player);
            return;
        }

        if (!itemStack.hasTag(ALGO_SELECTOR_TAG) && !itemStack.hasTag(ALGO_INTERACTION_TAG)) {
            return;
        }




        if (itemStack.getTag(ALGO_INTERACTION_TAG).equals(SPAWN)) {
            if(player.getInstance() == defaultInstance)
                return;
            if(instance != null)
                instance.removePlayer(defaultInstance, player);
            return;
        }


//        if (session == null) {
//            System.err.println("No controls for player " + player.getUsername());
//            return;
//        }

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

    public void selectAlgorithm(Player player) {
        final var inventory = ui.openSelector(algorithmRegistry.keySet(), presentation -> {
            var entry = presentations.get(presentation);
            return entry != null ? entry : new AlgorithmPresentation(presentation);
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer() != player) return;
            if (event.getInventory() != inventory) return;

            event.setCancelled(true); // Prevent taking items

            ItemStack clickedItem = event.getClickedItem();
            if (clickedItem.isAir()) return;

            final var algorithmId = clickedItem.getTag(ALGO_ID_TAG);
            if (algorithmId == null) return;

            final var entry = algorithmRegistry.get(algorithmId);
            if (entry == null) return;

            player.closeInventory();
            ui.applyRunningLayout(player);
            var session = createInstance(entry.id(), player);
            player.setInstance(session.getInstance())
                    .thenCompose(_ -> player.teleport(AlgorithmInstance.origin))
                    .thenCompose(_ -> session.startVisualization())
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        player.sendMessage(Component.text("Failed to initialize visualization", NamedTextColor.RED));
                        return null;
                    });


        });

        player.openInventory(inventory);
    }

    private final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();

    public static class PendingInvite {
        public final UUID inviter;
        public final AlgorithmInstance<?, ?, ?> instance;
        public final long expiresAt;

        public PendingInvite(UUID inviter, AlgorithmInstance<?, ?, ?> instance, long expiresAt) {
            this.inviter = inviter;
            this.instance = instance;
            this.expiresAt = expiresAt;
        }
    }

    public Map<UUID, PendingInvite> getPendingInvites() {
        return pendingInvites;
    }

    public Instance getDefaultInstance() {
        return defaultInstance;
    }
}
