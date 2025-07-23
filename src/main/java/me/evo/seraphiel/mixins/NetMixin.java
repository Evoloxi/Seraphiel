package me.evo.seraphiel.mixins;

import me.evo.seraphiel.Utils;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetMixin {
    @Inject(method = "handleScoreboardObjective", at = @At("HEAD"))
    private void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn, CallbackInfo ci) {
        Utils.receiveSidebarData();
    }
}
