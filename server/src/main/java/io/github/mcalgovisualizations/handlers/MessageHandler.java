package io.github.mcalgovisualizations.handlers;

import io.github.mcalgovisualizations.visualization.algorithms.Message;
import io.github.mcalgovisualizations.visualization.renderer.IAnimationHandler;
import io.github.mcalgovisualizations.visualization.renderer.dispatch.AnimationPlan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MessageHandler implements IAnimationHandler<Message> {
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