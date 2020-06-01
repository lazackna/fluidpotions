package me.crupette.fluidpotions.mixin;

import me.crupette.fluidpotions.FluidPotions;
import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Registry.class)
public class PotionRegistryMixin {

    @Inject(
            method = "register(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;",
            at = @At("TAIL")
    )
    private static <T> void onRegistryAdd(Registry<? super T> registry, Identifier id, T entry, CallbackInfoReturnable<T> ci){
        if(entry.getClass() == Potion.class){
            FluidPotions.registerPotion(id, (Potion)entry);
        }
    }
}
