package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.pov.SceneAdapterRegistry;
import io.github.mcalgovisualizations.visualization.renderer.scene.ISceneOps;
import io.github.mcalgovisualizations.visualization.instance.PlayerFeedback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public final class VillagerPovHandler {
    private final SceneAdapterRegistry adapters;
    private boolean enabled = false;

    public VillagerPovHandler(SceneAdapterRegistry adapters) {
        this.adapters = adapters;
    }

    public void toggle(Player player, ISceneOps scene, PlayerFeedback audience) {
        if (enabled) {
            disable(player, scene);
            return;
        }

        var adapter = adapters.resolve(scene).orElse(null);
        if (adapter == null) {
            return;
        }

        Entity cameraTarget = adapter.cameraTarget(scene);
        if (cameraTarget == null || !cameraTarget.isActive()) {
            audience.sendMessage(Component.text("Villager POV unavailable (no active villager)", NamedTextColor.GRAY));
            return;
        }

        player.spectate(cameraTarget);
        adapter.setLocatorBarVisible(scene, player, true);
        enabled = true;
    }

    public void refresh(Player player, ISceneOps scene, PlayerFeedback audience) {
        if (!enabled) {
            return;
        }

        var adapter = adapters.resolve(scene).orElse(null);
        if (adapter == null) {
            disable(player, scene);
            return;
        }

        Entity cameraTarget = adapter.cameraTarget(scene);
        if (cameraTarget == null || !cameraTarget.isActive()) {
            disable(player, scene);
            audience.sendMessage(Component.text("Villager POV disabled (no active villager)", NamedTextColor.GRAY));
            return;
        }

        player.spectate(cameraTarget);
        adapter.setLocatorBarVisible(scene, player, true);
    }

    public void disable(Player player, ISceneOps scene) {
        if (!enabled) {
            return;
        }

        adapters.resolve(scene).ifPresent(adapter -> adapter.setLocatorBarVisible(scene, player, false));
        player.stopSpectating();
        enabled = false;
    }

    public boolean supports(ISceneOps scene) {
        return adapters.supports(scene);
    }
}

