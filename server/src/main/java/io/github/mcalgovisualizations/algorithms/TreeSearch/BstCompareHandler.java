package io.github.mcalgovisualizations.algorithms.TreeSearch;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public final class BstCompareHandler implements IAnimationHandler<Compare> {

    private static final int SEARCHER_SLOT = -100; // A unique slot for the searcher villager

    @Override
    public AnimationPlan<TreeScene> handle(Compare event) {
        String narration = buildNarration(event);

        return AnimationPlan.<TreeScene>builder()
                .stepAsync(1, sceneOps -> {
                    // Handle the searcher villager
                    var searcher = sceneOps.getDisplay(SEARCHER_SLOT);

                    sceneOps.sendActionBar(Component.text(
                            "BST compare [" + event.xValue() + "] vs [" + event.yValue() + "]",
                            NamedTextColor.AQUA));
                    
                    // Only highlight if the display exists (avoid slot 0 error if it's -1 or missing)
                    if (event.x() != -1 && sceneOps.getDisplay(event.x()) != null) {
                        sceneOps.setHighlighted(event.x(), true);
                    }
                    
                    // Only highlight y if it's a valid slot (not -1) and exists
                    if (event.y() != -1 && sceneOps.getDisplay(event.y()) != null) {
                        sceneOps.setHighlighted(event.y(), true);
                    }
                    sceneOps.sendMessage(Component.text(narration, NamedTextColor.GRAY));

                    if (searcher == null) {
                        // Spawn the searcher at the current node's position
                        var startDisplay = sceneOps.getDisplay(event.x());
                        if (startDisplay != null) {
                            var startPos = startDisplay.getPos(); // Use node's position directly
                            sceneOps.addDisplay(SEARCHER_SLOT, new EntityCreatureDisplay(startPos, EntityType.VILLAGER, "Searcher", true));
                        }
                        return CompletableFuture.completedFuture(null);
                    } else {
                        // Walk the searcher to the current node's position and return the completion future
                        var targetDisplay = sceneOps.getDisplay(event.x());
                        if (targetDisplay != null) {
                            var targetPos = targetDisplay.getPos(); // Use node's position directly
                            return sceneOps.walkSlotTo(SEARCHER_SLOT, targetPos);
                        }
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .step(1, sceneOps -> {
                    if (event.x() != -1 && sceneOps.getDisplay(event.x()) != null) {
                        sceneOps.hoverDisplay(event.x(), true);
                    }
                    if (event.y() != -1 && sceneOps.getDisplay(event.y()) != null) {
                        sceneOps.hoverDisplay(event.y(), true);
                    }
                })
                .step(1, sceneOps -> {
                    if (event.x() != -1 && sceneOps.getDisplay(event.x()) != null) {
                        sceneOps.hoverDisplay(event.x(), false);
                    }
                    if (event.y() != -1 && sceneOps.getDisplay(event.y()) != null) {
                        sceneOps.hoverDisplay(event.y(), false);
                    }
                })
                .step(1, sceneOps -> {
                    if (event.x() != -1 && sceneOps.getDisplay(event.x()) != null) {
                        sceneOps.setHighlighted(event.x(), false);
                    }
                    if (event.y() != -1 && sceneOps.getDisplay(event.y()) != null) {
                        sceneOps.setHighlighted(event.y(), false);
                    }
                })
                .build();
    }

    private String buildNarration(Compare event) {
        if (!(event.xValue() instanceof Integer current) || !(event.yValue() instanceof Integer candidate)) {
            if (event.xValue() instanceof String currentText && event.yValue() instanceof String candidateText) {
                int cmp = candidateText.compareTo(currentText);
                if (cmp < 0) {
                    return "Go left of " + currentText;
                }
                if (cmp > 0) {
                    return "Go right of " + currentText;
                }
                return "Equal, go middle from " + currentText;
            }
            return "BST step";
        }

        if (candidate < current) {
            return "Go left from " + current;
        }
        if (candidate > current) {
            return "Go right from " + current;
        }

        return "Found " + candidate;
    }
}