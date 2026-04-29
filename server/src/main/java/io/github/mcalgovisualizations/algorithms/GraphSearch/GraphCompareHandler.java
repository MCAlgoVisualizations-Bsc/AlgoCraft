package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.events.Compare;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class GraphCompareHandler implements IAnimationHandler<Compare> {

    private static final int SEARCHER_SLOT = -100;

    @Override
    public AnimationPlan<GraphScene> handle(Compare event) {
        return AnimationPlan.<GraphScene>builder()
                .stepAsync(1, sceneOps -> {
                    var searcher = sceneOps.getDisplay(SEARCHER_SLOT);
                    var targetDisplay = sceneOps.getDisplay(event.x());

                    if (targetDisplay == null) return CompletableFuture.completedFuture(null);

                    // --- PRINT TO CHAT ---
                    // This sends a message to all players in the instance
                    String msg = "Villager moving to Node: " + event.xValue();
                    sceneOps.sendMessage(Component.text(msg, NamedTextColor.YELLOW));

                    // Also update the Action Bar for the current target
                    sceneOps.sendActionBar(Component.text("Target Node: " + event.yValue(), NamedTextColor.GOLD));

                    if (searcher == null) {
                        sceneOps.addDisplay(SEARCHER_SLOT, new EntityCreatureDisplay(
                                targetDisplay.getPos(), EntityType.VILLAGER, "Searcher", true));
                        return CompletableFuture.completedFuture(null);
                    } else {
                        // The villager walks. Since the search emits nodes in order,
                        // he will follow the path back and forth.
                        return sceneOps.walkSlotTo(SEARCHER_SLOT, targetDisplay.getPos())
                                .thenRun(() -> {
                                    // Drop bread after reaching the node
                                    ItemStack breadItem = ItemStack.builder(Material.BREAD)
                                            .amount(1)
                                            .build();
                                    ItemEntity breadEntity = new ItemEntity(breadItem);
                                    
                                    // Make the bread unpickable by setting a very long pickup delay
                                    breadEntity.setPickupDelay(Duration.ofDays(365));
                                    
                                    // Cast sceneOps to GraphScene to access the instance
                                    GraphScene graphScene = (GraphScene) sceneOps;
                                    breadEntity.setInstance(graphScene.getInstance(), targetDisplay.getPos().add(0, 0.5, 0));
                                    
                                    // Register the bread entity for cleanup
                                    graphScene.addItemEntity(breadEntity);
                                });
                    }
                })
                .build();
    }
}
