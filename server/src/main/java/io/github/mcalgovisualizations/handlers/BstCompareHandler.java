package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityType;

public final class BstCompareHandler implements IAnimationHandler<Compare> {

    private static final int SEARCHER_SLOT = -100; // A unique slot for the searcher villager

    @Override
    public AnimationPlan handle(Compare event) {
        String narration = buildNarration(event);

        return AnimationPlan.builder()
                .step(1, sceneOps -> {
                    // Handle the searcher villager
                    var searcher = sceneOps.getDisplay(SEARCHER_SLOT);
                    if (searcher == null) {
                        // Spawn the searcher at the current node's position
                        var startDisplay = sceneOps.getDisplay(event.x());
                        if (startDisplay != null) {
                            var startPos = startDisplay.getPos().add(0, 2, 0); // Offset above the node
                            sceneOps.addDisplay(SEARCHER_SLOT, new EntityCreatureDisplay(startPos, EntityType.VILLAGER, "Searcher"));
                        }
                    } else {
                        // Move the searcher to the current node's position
                        var targetDisplay = sceneOps.getDisplay(event.x());
                        if (targetDisplay != null) {
                            var targetPos = targetDisplay.getPos().add(0, 2, 0); // Offset above the node
                            sceneOps.moveSlotTo(SEARCHER_SLOT, targetPos);
                        }
                    }

                    sceneOps.sendActionBar(Component.text(
                            "BST compare [" + event.xValue() + "] vs [" + event.yValue() + "]",
                            NamedTextColor.AQUA));
                    sceneOps.setHighlighted(event.x(), true);
                    // Only highlight y if it's a valid slot (not -1)
                    if (event.y() != -1) {
                        sceneOps.setHighlighted(event.y(), true);
                    }
                    sceneOps.sendMessage(Component.text(narration, NamedTextColor.GRAY));
                })
                .step(1, sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), true);
                    if (event.y() != -1) {
                        sceneOps.hoverDisplay(event.y(), true);
                    }
                })
                .step(1, sceneOps -> {
                    sceneOps.hoverDisplay(event.x(), false);
                    if (event.y() != -1) {
                        sceneOps.hoverDisplay(event.y(), false);
                    }
                })
                .step(1, sceneOps -> {
                    sceneOps.setHighlighted(event.x(), false);
                    if (event.y() != -1) {
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
