package io.github.mcalgovisualizations.algorithms.GraphSearch;

import io.github.mcalgovisualizations.Displays.EntityCreatureDisplay;
import io.github.mcalgovisualizations.visualization.renderer.LayoutResult;
import io.github.mcalgovisualizations.visualization.renderer.scene.AbstractScene;
import io.github.mcalgovisualizations.visualization.renderer.scene.SceneContext;
import io.github.mcalgovisualizations.visualization.renderer.scene.VillagerPOV;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GraphScene extends AbstractScene implements VillagerPOV {
    private static final int SEARCHER_SLOT = -100;
    private final List<ItemEntity> extraEntities = new ArrayList<>();

    public GraphScene(@NotNull SceneContext context) {
        super(context);
    }

    public Instance getInstance() {
        return this.instance;
    }

    public void addItemEntity(ItemEntity entity) {
        extraEntities.add(entity);
    }

    public Entity cameraTarget() {
        var display = getDisplay(SEARCHER_SLOT);
        if (display instanceof EntityCreatureDisplay creatureDisplay) {
            return creatureDisplay.getEntity();
        }
        return null;
    }

    @Override
    public void setLayout(LayoutResult[] layoutResults) {
        for (int i = 0; i < layoutResults.length; i++) {
            if (layoutResults[i] != null) {
                addDisplay(i, layoutResults[i].displayValue());
            }
        }
    }

    @Override
    public void cleanUp() {
        for (ItemEntity entity : extraEntities) {
            entity.remove();
        }
        extraEntities.clear();
        super.cleanUp();
    }
}
