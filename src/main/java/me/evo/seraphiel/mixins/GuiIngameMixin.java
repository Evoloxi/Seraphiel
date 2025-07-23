package me.evo.seraphiel.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.weavemc.loader.api.event.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class GuiIngameMixin {

    
/*
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiIngame;)Lnet/minecraft/client/gui/GuiPlayerTabOverlay;"))
    public GuiPlayerTabOverlay onNewGuiPlayerTabOverlay(Minecraft minecraft, GuiIngame guiIngame) {
        return new GuiPlayerTabOverride(minecraft, guiIngame);
    }
*/

/*    @Inject(method = "renderGameOverlay", at = @At(value = "HEAD"))
    public void onRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        EventBus.postEvent(new RenderGameOverlayEvent.Pre(partialTicks));
    }

    @Inject(method = "renderGameOverlay", at = @At(value = "RETURN"))
    public void onRenderGameOverlayPost(float partialTicks, CallbackInfo ci) {
        EventBus.postEvent(new RenderGameOverlayEvent.Post(partialTicks));
    }*/
}
