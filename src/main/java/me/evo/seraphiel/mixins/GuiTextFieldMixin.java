package me.evo.seraphiel.mixins;

import me.evo.seraphiel.command.CommandManager;
import me.evo.seraphiel.command.PlayerCommandSource;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiChat.class)
public class GuiTextFieldMixin {
    @Shadow protected GuiTextField inputField;

    @Inject(method = "keyTyped", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiChat;autocompletePlayerNames()V",
            shift = At.Shift.AFTER
    ), cancellable = true)
    public void complete(char c, int i, CallbackInfo ci) {
        List<String> auto = CommandManager.INSTANCE.tryAutoComplete(inputField.getText(), new PlayerCommandSource());
        if (!auto.isEmpty()) {
            String prefix = auto.get(0);
            for (String s : auto) {
                int j = 0;
                while (j < prefix.length() && j < s.length() && prefix.charAt(j) == s.charAt(j)) {
                    j++;
                }
                prefix = prefix.substring(0, j);
            }
            inputField.setText(prefix);
            inputField.setCursorPositionEnd();
            ci.cancel();
        }
    }

}
