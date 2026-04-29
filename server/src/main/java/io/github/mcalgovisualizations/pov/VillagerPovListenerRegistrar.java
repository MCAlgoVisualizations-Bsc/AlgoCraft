package io.github.mcalgovisualizations.pov;

import io.github.mcalgovisualizations.ILocatorBarScene;
import io.github.mcalgovisualizations.visualization.instance.AlgoCraft;
import io.github.mcalgovisualizations.handlers.VillagerPovHandler;
import io.github.mcalgovisualizations.visualization.ui.InteractionType;
import io.github.mcalgovisualizations.visualization.instance.PlayerFeedback;
import io.github.mcalgovisualizations.ui.VillagerPovAlgorithmUI;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;

public final class VillagerPovListenerRegistrar {
    private final AlgoCraft algo;
    private final VillagerPovHandler villagerPovHandler;

    public VillagerPovListenerRegistrar(AlgoCraft algo, VillagerPovHandler villagerPovHandler) {
        this.algo = algo;
        this.villagerPovHandler = villagerPovHandler;
    }

    public void register(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> onPlayerUseItem(event.getPlayer(), event));
        handler.addListener(PlayerDisconnectEvent.class, event -> onDisconnect(event.getPlayer()));
    }

    private void onPlayerUseItem(Player player, PlayerUseItemEvent event) {
        var itemStack = event.getItemStack();

        if (itemStack.hasTag(VillagerPovAlgorithmUI.POV_TOGGLE_TAG)) {
            algo.sceneFor(player).ifPresent(scene -> villagerPovHandler.toggle(player, scene, new PlayerFeedback(() -> player)));
            return;
        }

        if (itemStack.hasTag(io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_INTERACTION_TAG)) {
            var interaction = itemStack.getTag(io.github.mcalgovisualizations.visualization.ui.Tags.ALGO_INTERACTION_TAG);

            if (interaction == InteractionType.START || interaction == InteractionType.RANDOMIZE) {
                algo.sceneFor(player).ifPresent(scene -> {
                    if (scene instanceof ILocatorBarScene locatorScene) {
                        locatorScene.initializeLocatorBar();
                    }
                    villagerPovHandler.refresh(player, scene, new PlayerFeedback(() -> player));
                });
            }
        }
    }

    private void onDisconnect(Player player) {
        algo.sceneFor(player).ifPresent(scene -> villagerPovHandler.disable(player, scene));
    }
}


