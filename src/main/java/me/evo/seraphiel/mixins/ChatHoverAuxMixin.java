package me.evo.seraphiel.mixins;

import me.evo.seraphiel.gui.HoverStats;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiChat.class)
public class ChatHoverAuxMixin {

    @Redirect(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;getChatComponent(II)Lnet/minecraft/util/IChatComponent;",
                    ordinal = 0
            )
    )
    private IChatComponent modifyChatComponent(GuiNewChat instance, int i, int i1) {
        IChatComponent par1 = instance.getChatComponent(i, i1);
        return HoverStats.updateHovered(par1);
    }
}
