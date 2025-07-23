package me.evo.seraphiel.mixins;

import me.evo.seraphiel.command.CommandManager;
import me.evo.seraphiel.command.PlayerCommandSource;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
    @Inject(method = "sendChatMessage" , at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("/") && CommandManager.INSTANCE.getCommand(message.substring(1)) != null) {
            ci.cancel();
            CommandManager.executeSafe(message.substring(1), new PlayerCommandSource(), CommandManager.INSTANCE.getDefaultHander());
        }
    }
}
