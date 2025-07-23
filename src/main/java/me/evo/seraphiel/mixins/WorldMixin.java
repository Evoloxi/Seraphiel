package me.evo.seraphiel.mixins;

import me.evo.seraphiel.event.EntityListEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.weavemc.loader.api.event.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    @Inject(method = "spawnEntityInWorld", at = @At("HEAD"))
    public void onSpawnEntityInWorld(Entity en, CallbackInfoReturnable<Boolean> cir) {
        EventBus.postEvent(new EntityListEvent.Add(en));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    public void onRemoveEntity(Entity en, CallbackInfo ci) {
        EventBus.postEvent(new EntityListEvent.Remove(en));
    }
}
