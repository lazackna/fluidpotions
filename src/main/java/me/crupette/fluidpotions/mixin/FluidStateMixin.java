package me.crupette.fluidpotions.mixin;

import me.crupette.fluidpotions.fluid.PotionFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidState.class)
public abstract class FluidStateMixin {

    @Shadow
    public abstract Fluid getFluid();

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void matchesPotionFluid(Tag<Fluid> tag, CallbackInfoReturnable<Boolean> ci){
        if(tag.equals(FluidTags.WATER) && getFluid() instanceof PotionFluid) ci.setReturnValue(true);
    }

}
