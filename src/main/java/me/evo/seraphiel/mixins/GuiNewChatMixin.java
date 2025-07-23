package me.evo.seraphiel.mixins;

import me.evo.seraphiel.event.ChatEvent;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.weavemc.loader.api.event.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
    public void onReceiveChat(IChatComponent c, int v, CallbackInfo ci) {
        EventBus.postEvent(new ChatEvent.Receive(c));
    }
}
