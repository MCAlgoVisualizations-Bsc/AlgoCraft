package io.github.mcalgovisualizations.prefab.handlers;

import io.github.mcalgovisualizations.prefab.events.Message;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Converts message events into simple text output.
 */
public class MessageHandler implements IAnimationHandler<Message> {
    /**
     * Builds the animation plan for a message event.
     *
     * @param event message event to render
     * @return animation plan that sends the message to the scene
     */
    @Override
    public AnimationPlan handle(Message event) {
        NamedTextColor color = switch (event.type()) {
            case INFO    -> NamedTextColor.GRAY;
            case SUCCESS -> NamedTextColor.GREEN;
            case ERROR   -> NamedTextColor.RED;
            case HINT    -> NamedTextColor.AQUA;
        };
        Component component = Component.text(event.message(), color);
        return AnimationPlan.instant(scene -> scene.sendMessage(component));
    }
}