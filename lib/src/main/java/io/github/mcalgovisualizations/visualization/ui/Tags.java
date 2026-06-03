package io.github.mcalgovisualizations.visualization.ui;

import net.minestom.server.tag.Tag;

public class Tags {
    /** Tag storing the selected algorithm id. */
    public static final Tag<String> ALGO_ID_TAG = Tag.String("algo_id");
    /** Tag storing the current running interaction type. */
    public static final Tag<InteractionType> ALGO_INTERACTION_TAG = Tag.String("algo_interaction")
            .map(InteractionType::valueOf, InteractionType::name);
    /** Tag marking the algorithm selector item. */
    public static final Tag<Boolean> ALGO_SELECTOR_TAG = Tag.Boolean("algo_select");
    /** Tag marking the villager POV toggle item. */
    public static final Tag<Boolean> VILLAGER_POV_TAG = Tag.Boolean("villager_pov");
}
