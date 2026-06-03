package io.github.mcalgovisualizations.visualization.instance;

import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmUI;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.scene.VillagerPOV;
import io.github.mcalgovisualizations.visualization.ui.Tags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.github.mcalgovisualizations.visualization.ui.InteractionType.SPAWN;
import static io.github.mcalgovisualizations.visualization.ui.Tags.*;

public class AlgoCraft {
    private final Map<String, Algorithm<?,?,?>> algorithmRegistry = new HashMap<>();
    // player uuid for fast lookup
    private final Map<UUID, AlgorithmInstance<?,?,?>> playerInstance = new HashMap<>();
    private final Map<UUID, PlayerInventory> playerInventory = new HashMap<>();
    private final Set<UUID> enabledPOV = new HashSet<>();
    private final AlgorithmUI ui = new AlgorithmUI();
    private final Instance defaultInstance;
    public final static Pos SPAWN_POS = new Pos(194.5, 137, -38.5);

    /**
     * Creates a new AlgoCraft session manager bound to the provided hub instance.
     *
     * @param defaultInstance the default instance players return to when leaving a visualization
     */
    public AlgoCraft(InstanceContainer defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    /**
     * Returns the hub instance used as the default spawn location.
     *
     * @return the default instance
     */
    public Instance getDefaultInstance() {
        return defaultInstance;
    }

    /**
     * Creates a runnable visualization session for the algorithm with the given id.
     *
     * @param id the registered algorithm id
     * @param players players that should be attached to the session immediately
     * @return the created algorithm session
     * @throws IllegalArgumentException if no algorithm with the given id exists
     */
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

    /**
     * Removes the given players from their current visualization and returns them to the hub.
     *
     * @param players players to remove from the active session
     */
    public void removePlayerFromInstance(Player... players) {
        for(var player : players) {
            var instance = requireInstance(player);
            instance.removePlayer(defaultInstance, player)
                    .thenRun(() -> ui.applyDefaultLayout(player, getDefaultInstance()));
        }
    }

    /**
     * Looks up the active scene for a player, if one exists.
     *
     * @param player the player to inspect
     * @return the active scene, or an empty optional if the player is not in a session
     */
    public Optional<ISceneOps> sceneFor(Player player) {
        var instance = playerInstance.get(player.getUuid());
        if (instance == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ISceneOps) instance.getScene());
    }

    /**
     * Registers an algorithm so it appears in the selector UI.
     *
     * @param algorithm the algorithm definition to register
     * @param <T> model value type
     * @param <C> algorithm context type
     */
    public <T, C extends AlgorithmContext<T>> void registerAlgorithm(Algorithm<T, C, ?> algorithm) {
        algorithmRegistry.put(algorithm.id(), algorithm);
    }

    /**
     * Returns the active session for a player.
     *
     * @param player the player to inspect
     * @return the active algorithm session, or {@code null} if the player is not in one
     */
    public AlgorithmInstance<?,?,?> requireInstance(Player player) {
        return playerInstance.get(player.getUuid());
    }

    /**
     * Gives the player the default hub inventory layout.
     *
     * @param player the player to update
     */
    public void applyDefaultLayout(Player player) {
        ui.applyDefaultLayout(player, getDefaultInstance());
    }

    /**
     * Registers the global item interaction listeners needed by AlgoCraft.
     *
     * @param handler the global event handler to attach listeners to
     */
    public void addListener(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, this::onPlayerUseItem);

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
        
        if (itemStack.hasTag(Tags.VILLAGER_POV_TAG)) {
            toggleVillagerPOV(player, instance);
            return;
        }

        if (!itemStack.hasTag(ALGO_SELECTOR_TAG) && !itemStack.hasTag(ALGO_INTERACTION_TAG)) {
            return;
        }

        if (itemStack.getTag(ALGO_INTERACTION_TAG).equals(SPAWN)) {
            if(player.getInstance() == defaultInstance)
                return;

            if(instance != null) {
                instance.removePlayer(defaultInstance, player).thenRun(() -> MinecraftServer.getInstanceManager().unregisterInstance(instance.getInstance()));

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
                instance.removePlayer(defaultInstance, player)
                        .thenRun(() -> MinecraftServer.getInstanceManager().unregisterInstance(instance.getInstance()))
                        .thenRun(() -> playerInventory.remove(player.getUuid()))
                        .thenRun(() -> playerInstance.remove(player.getUuid()))
                        .thenRun(() -> instance.removePlayer(defaultInstance, player));
                ui.applyDefaultLayout(player, getDefaultInstance());
            }
        }
    }

    /**
     * Toggles the special villager camera mode for a player.
     *
     * @param player the player toggling POV
     * @param instance the active visualization session
     */
    private void toggleVillagerPOV(Player player, AlgorithmInstance<?, ?, ?> instance) {
        if (instance == null) {
            player.sendMessage(Component.text("No active algorithm", NamedTextColor.RED));
            return;
        }

        var scene = instance.getScene();
        if (!(scene instanceof VillagerPOV povScene)) {
            // Silently ignore - shouldn't happen if item setup is correct
            return;
        }

        UUID playerId = player.getUuid();

        if (enabledPOV.contains(playerId)) {
            // Disable POV
            if (player.getVehicle() != null) {
                player.getVehicle().removePassenger(player);
            }
            player.setInvisible(false);
            povScene.setLocatorBarVisible(player, false);
            povScene.onPovToggle(player, false);
            enabledPOV.remove(playerId);
            player.sendMessage(Component.text("POV disabled", NamedTextColor.GRAY));
        } else {
            // Enable POV
            Entity cameraTarget = povScene.cameraTarget();
            if (cameraTarget == null || !cameraTarget.isActive()) {
                player.sendMessage(Component.text("Villager POV unavailable (no active villager)", NamedTextColor.GRAY));
                return;
            }

            player.setInvisible(true);
            cameraTarget.addPassenger(player);
            povScene.setLocatorBarVisible(player, true);
            povScene.onPovToggle(player, true);
            enabledPOV.add(playerId);
            player.sendMessage(Component.text("POV enabled", NamedTextColor.YELLOW));
        }
    }

    /**
     * Opens the algorithm selector GUI for a player.
     *
     * @param player the player opening the selector
     */
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
                    ui.applyDefaultLayout(player, getDefaultInstance());
                    player.sendMessage(Component.text("Failed to initialize visualization", NamedTextColor.RED));
                    return null;
                })
                .thenRun(() -> entry.runningLayout().applyRunningLayout(player, entry.supportsPOV()));
        });

        player.openInventory(inventory);
    }

    private final Map<UUID, Set<PendingInvite>> pendingInvites = new HashMap<>();

    /**
     * Represents a pending invitation to join a visualization session.
     *
     * @param inviter the inviting player id
     * @param instance the target session
     * @param expiresAt the expiration timestamp in epoch milliseconds
     */
    public record PendingInvite(UUID inviter, AlgorithmInstance<?, ?, ?> instance, long expiresAt) {
        /**
         * Returns whether this invite has expired.
         *
         * @return {@code true} if the invite is expired
         */
        public boolean isExpired() {
                return System.currentTimeMillis() > expiresAt;
        }
        /**
         * Returns the remaining time before the invite expires.
         *
         * @return milliseconds remaining until expiration
         */
        public long remainingMillis() {
            return Math.max(0, expiresAt - System.currentTimeMillis());
        }
        @Override
        public @NonNull String toString() {
            long totalSeconds = remainingMillis() / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            return minutes > 0
                    ? String.format("%dm %02ds", minutes, seconds)
                    : seconds + "s";
        }
    }

    /**
     * Returns all active invites targeting the given player.
     *
     * @param player the player to inspect
     * @return immutable set of active invites
     */
    public Set<PendingInvite> getPendingInvites(Player player) {
        var invites = pendingInvites.getOrDefault(player.getUuid(), new HashSet<>());
        invites.removeIf(invite -> invite.isExpired() || !invite.instance.getInstance().isRegistered());
        return Set.copyOf(invites);
    }

    /**
     * Sends an invite from one player to another.
     *
     * @param inviter the player issuing the invite
     * @param target the player receiving the invite
     * @param expiresIn expiration timestamp in epoch milliseconds
     */
    public void invitePlayer(Player inviter, Player target, long expiresIn) {
        final var inviterInstance = requireInstance(inviter);

        if(inviter.getInstance().equals(defaultInstance) || inviterInstance == null) {
            inviter.sendMessage(Component.text("You have to be in a visualization before inviting others", NamedTextColor.RED));
            return;
        }

        if(inviter.getInstance().equals(target.getInstance())) {
            inviter.sendMessage(Component.text("You cannot invite players from the same instance", NamedTextColor.RED));
            return;
        }

        inviter.sendMessage(Component.text("You have invited " + target.getUsername() + " to your instance."));
        target.sendMessage(Component.text("You have been invited to join: " + inviterInstance.getPresentation().algorithmId(), NamedTextColor.GREEN));
        final var invites = pendingInvites.computeIfAbsent(target.getUuid(), _ -> new HashSet<>());
        invites.add(new PendingInvite(inviter.getUuid(), inviterInstance, expiresIn));
    }

    /**
     * Accepts an invite sent by a specific player.
     *
     * @param invited the player accepting the invite
     * @param inviter the player who sent the invite
     * @return {@code true} if an invite was accepted
     */
    public boolean acceptInvite(Player invited, Player inviter) {
        var inviteList = pendingInvites.computeIfAbsent(invited.getUuid(), _ -> new HashSet<>());
        inviteList.removeIf(invite -> invite.isExpired() || !invite.instance.getInstance().isRegistered());

        if(inviteList.stream().noneMatch(invite -> invite.inviter().equals(inviter.getUuid())))
            invited.sendMessage(Component.text("You have no active invite sent to you from: " + inviter.getUsername(), NamedTextColor.RED));

        for(var invite : inviteList) {
            if(!invite.inviter().equals(inviter.getUuid()))
                continue;
            var instance = requireInstance(inviter);
            this.playerInstance.put(invited.getUuid(), instance);
            instance.addPlayer(invited);
            inviteList.remove(invite);
            return true;
        }

        return false;
    }
}
