package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.commands.Accept;
import io.github.mcalgovisualizations.visualization.commands.Invite;
import io.github.mcalgovisualizations.visualization.commands.PendingInvites;
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

import java.util.*;

import static io.github.mcalgovisualizations.visualization.ui.InteractionType.SPAWN;
import static io.github.mcalgovisualizations.visualization.ui.Tags.*;

public class AlgoCraft {
    private final Map<String, Algorithm<?,?,?>> algorithmRegistry = new HashMap<>();
    // player uuid for fast lookup
    private final Map<UUID, AlgorithmInstance<?,?,?>> playerInstance = new HashMap<>();
    private final Map<UUID, PlayerInventory> playerInventory = new HashMap<>();
    private final AlgorithmUI ui = new AlgorithmUI();
    private final Instance defaultInstance;
    public final static Pos SPAWN_POS = new Pos(194.5, 137, -38.5);

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
            instance.removePlayer(defaultInstance, player)
                    .thenRun(() -> ui.applyDefaultLayout(player));
        }
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
        MinecraftServer.getCommandManager().register(new PendingInvites(this));
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

            if(instance != null) {
                instance.removePlayer(defaultInstance, player);
            }
            return;
        }

        final var controller = instance.getController();
        switch (itemStack.getTag(ALGO_INTERACTION_TAG)) {
            case RANDOMIZE -> controller.randomize();
            case START -> controller.start();
            case STOP -> controller.pause();
            case FORWARD -> controller.step();
            case BACKWARD -> controller.back();
            case SET_SPEED -> controller.changeSpeed();
            default -> {
                controller.clear();
                ui.applyDefaultLayout(player);
            }
        }
    }

    public void selectAlgorithm(Player player) {
        final var inventory = ui.openSelector(algorithmRegistry.keySet(), presentation -> {
            var entry = algorithmRegistry.get(presentation).presentation();
            return entry != null ? entry : new AlgorithmPresentation(presentation);
        });

        // TODO : this shouldn't be nested
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
            var session = createInstance(entry.id(), player);

            session.startVisualization()
                .thenCompose(_ -> session.addPlayer(player))
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    player.sendMessage(Component.text("Failed to initialize visualization", NamedTextColor.RED));
                    return null;
                })
                .thenRun(() -> ui.applyRunningLayout(player));
        });

        player.openInventory(inventory);
    }

    private final Map<UUID, Set<PendingInvite>> pendingInvites = new HashMap<>();

    public record PendingInvite(UUID inviter, AlgorithmInstance<?, ?, ?> instance, long expiresAt) {
        public boolean isExpired() {
                return System.currentTimeMillis() > expiresAt;
        }
    }

    public Set<PendingInvite> getPendingInvites(Player player) {
        var invites = pendingInvites.getOrDefault(player.getUuid(), new HashSet<>());

        invites.removeIf(PendingInvite::isExpired);
        return Set.copyOf(invites);
    }

    public void invitePlayer(Player inviter, Player target, long expiresIn) {
        final var inviterInstance = requireInstance(inviter);
        if(inviter == target) {
            target.sendMessage(Component.text("You cannot invite yourself", NamedTextColor.RED));
            return;
        }

        if(inviter.getInstance().equals(defaultInstance) || inviterInstance == null) {
            target.sendMessage(Component.text("You have to be in a visualization before inviting others", NamedTextColor.RED));
            return;
        }

        if(inviter.getInstance().equals(target.getInstance())) {
            target.sendMessage(Component.text("You cannot invite players from the same instance", NamedTextColor.RED));
            return;
        }


        target.sendMessage(Component.text("You have been invited to join: ", NamedTextColor.GREEN));
        final var invites = pendingInvites.computeIfAbsent(target.getUuid(), _ -> new HashSet<>());
        invites.add(new PendingInvite(inviter.getUuid(), inviterInstance, expiresIn));
    }

    public boolean acceptInvite(Player invited, Player inviter) {
        var inviteList = pendingInvites.computeIfAbsent(invited.getUuid(), _ -> new HashSet<>());
        inviteList.removeIf(PendingInvite::isExpired);

        if(inviteList.stream().noneMatch(invite -> invite.inviter().equals(inviter.getUuid())))
            invited.sendMessage(Component.text("You have no active invite sent to you from: " + inviter.getUsername(), NamedTextColor.RED));

        for(var invite : inviteList) {
            if(!invite.inviter().equals(inviter.getUuid()))
                continue;
            var instance = requireInstance(inviter);
            instance.addPlayer(invited);
            inviteList.remove(invite);
            return true;
        }

        return false;
    }

    public Instance getDefaultInstance() {
        return defaultInstance;
    }
}
