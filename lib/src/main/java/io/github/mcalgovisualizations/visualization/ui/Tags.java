package io.github.mcalgovisualizations.visualization.ui;

import net.minestom.server.tag.Tag;

public class Tags {
    public static final Tag<String> ALGO_ID_TAG = Tag.String("algo_id");
    public static final Tag<InteractionType> ALGO_INTERACTION_TAG = Tag.String("algo_interaction")
            .map(InteractionType::valueOf, InteractionType::name);
    public static final Tag<Boolean> ALGO_SELECTOR_TAG = Tag.Boolean("algo_select");
}
