package io.github.mcalgovisualizations;

import io.github.mcalgovisualizations.algorithms.*;
import io.github.mcalgovisualizations.algorithms.context.GridContext;
import io.github.mcalgovisualizations.algorithms.context.SortingContext;
import io.github.mcalgovisualizations.commands.*;
import io.github.mcalgovisualizations.config.MapConstants;
import io.github.mcalgovisualizations.handlers.*;
import io.github.mcalgovisualizations.layouts.*;
import io.github.mcalgovisualizations.visualization.AlgoCraft;
import io.github.mcalgovisualizations.visualization.Algorithm;
import io.github.mcalgovisualizations.events.CellStateTransition;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.events.Message;
import io.github.mcalgovisualizations.events.Swap;
import io.github.mcalgovisualizations.visualization.algorithm.ContextFactory;
import io.github.mcalgovisualizations.visualization.models.AlgorithmContext;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import io.github.mcalgovisualizations.visualization.renderer.scene.DefaultScene;
import io.github.mcalgovisualizations.visualization.ui.AlgorithmPresentation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.Material;

import java.util.*;
import java.util.function.Consumer;

import static io.github.mcalgovisualizations.RegisterAlgo.*;
import static io.github.mcalgovisualizations.config.MapConstants.*;
import static io.github.mcalgovisualizations.config.WorldConfig.createMainInstance;

public final class Main {
    private static AlgoCraft algo = null;

    static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init(new Auth.Online());
        InstanceContainer instance = createMainInstance();

        // Sets the game time
        instance.setTimeRate(0);  // Stops time
        instance.setTime(6000);   // Sets time to noon

        algo = new AlgoCraft(instance);

        registerAlgo(algo);

        algo.setSpawnAction(player -> player.teleport(HUB_SPAWN));
        algo.addListeners(MinecraftServer.getGlobalEventHandler());

        // Register visualization control listeners (item interactions)
        registerListeners(instance);
        // registerControls(instance, algo.visualizationManager);
        registerCommands(MinecraftServer.getCommandManager());

        server.start("0.0.0.0", 25565);
    }

    static void registerListeners(InstanceContainer instance) {
        final var globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Player configuration - set spawn instance and respawn point
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerSkin skin = PlayerSkin.fromUsername(player.getUsername());
            if (skin != null) {
                player.setSkin(skin);
            }
            event.setSpawningInstance(instance);
            player.setRespawnPoint(HUB_SPAWN);
        });

        // Player spawn - give items and assign visualization (player is now fully in the world)
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return; // Only on first spawn

            Player player = event.getPlayer();

            // Give fly access to player
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlying(true);

            // Library UI owns default hotbar layout (selector + spawn item)
            algo.applyDefaultLayout(player);

            // Send welcome message
            player.sendMessage(Component.text(
                    "Right-click the Nether Star to select an algorithm to visualize!", NamedTextColor.YELLOW));
            player.sendMessage(Component.text(
                    "Welcome to Algorithm Visualizations!", Message.MessageType.SUCCESS.color()));

        });

        // Cleanup visualization when player disconnects
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            // VisualizationManager.removeVisualization(event.getPlayer());
        });

    }



    static void registerCommands(CommandManager cm) {
        cm.register(new Greet());
        cm.register(new Teleport());
        cm.register(new Gamemode());
        cm.register(new Spawn());
    }
}