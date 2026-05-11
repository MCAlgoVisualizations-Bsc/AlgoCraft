package io.github.mcalgovisualizations.algorithms.mazes.Scenes;

import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.renderer.scene.VillagerPOV;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class HeuristicGridScene extends GridScene implements VillagerPOV, ILocatorBarScene {

    private final Set<Player> locatorViewers = new HashSet<>();
    private BossBar locatorBar = null;
    private int initialDistance = -1;
    private int previousDistance = -1;

    public HeuristicGridScene(SceneContext context) {
        super(context);
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        super.setLayout(layoutResults);
        // Only initialize the bar if the superclass successfully built a block grid
        if (isAStarGrid(layoutResults)) {
            initializeLocatorBar();
        }
    }

    @Override
    public void cleanUp() {
        clearLocatorBar();
        initialDistance = -1;
        previousDistance = -1;
        super.cleanUp();
    }

    @Override
    public void initializeLocatorBar() {
        if (layoutResults == null || startSlot < 0 || goalSlot < 0) {
            return;
        }

        if (locatorBar != null) {
            for (Player viewer : locatorViewers) {
                viewer.hideBossBar(locatorBar);
            }
        }

        initialDistance = manhattanDistance(startSlot, goalSlot);
        previousDistance = initialDistance;

        float progress = progressForDistance(initialDistance);
        locatorBar = BossBar.bossBar(locatorTitle(initialDistance), progress, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);

        for (Player viewer : locatorViewers) {
            viewer.showBossBar(locatorBar);
        }
    }

    @Override
    public void updateLocatorBar(int activeSlot) {
        if (locatorBar == null || layoutResults == null || goalSlot < 0 || activeSlot < 0 || activeSlot >= layoutResults.length) {
            return;
        }

        int currentDistance = manhattanDistance(activeSlot, goalSlot);
        float progress = progressForDistance(currentDistance);
        locatorBar.progress(progress);
        locatorBar.name(locatorTitle(currentDistance));

        if (previousDistance < 0 || currentDistance == previousDistance) {
            locatorBar.color(BossBar.Color.YELLOW);
        } else if (currentDistance < previousDistance) {
            locatorBar.color(BossBar.Color.GREEN);
        } else {
            locatorBar.color(BossBar.Color.RED);
        }

        previousDistance = currentDistance;
    }

    @Override
    public void setLocatorBarVisible(Player player, boolean visible) {
        if (player == null) {
            return;
        }

        if (visible) {
            locatorViewers.add(player);
            if (locatorBar != null) {
                player.showBossBar(locatorBar);
            }
            return;
        }

        locatorViewers.remove(player);
        if (locatorBar != null) {
            player.hideBossBar(locatorBar);
        }
    }

    @Override
    public void clearLocatorBar() {
        if (locatorBar != null) {
            for (Player viewer : locatorViewers) {
                viewer.hideBossBar(locatorBar);
            }
            locatorBar = null;
        }
        locatorViewers.clear();
    }

    private int manhattanDistance(int fromSlot, int toSlot) {
        int fromRow = fromSlot / inferredColumns;
        int fromCol = fromSlot % inferredColumns;
        int toRow = toSlot / inferredColumns;
        int toCol = toSlot % inferredColumns;
        return Math.abs(fromRow - toRow) + Math.abs(fromCol - toCol);
    }

    private float progressForDistance(int currentDistance) {
        if (initialDistance <= 0) {
            return 1.0f;
        }
        float raw = 1.0f - (currentDistance / (float) initialDistance);
        return Math.clamp(raw, 0.0f, 1.0f);
    }

    private Component locatorTitle(int currentDistance) {
        int initial = Math.max(initialDistance, 0);
        return Component.text("Manhatten Distance to Goal: " + currentDistance + " / " + initial + " (heuristic, ignoring walls)");
    }
}