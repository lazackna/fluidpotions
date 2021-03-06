package me.crupette.fluidpotions.mixin;

import me.crupette.fluidpotions.FluidPotions;
import me.crupette.fluidpotions.fluid.PotionFluid;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Inject(
            method = "use",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemUsage;method_30012(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void useOnPotionFluid(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> ci,
                                  ItemStack itemStack, BlockHitResult blockHitResult,
                                  BlockPos blockPos, BlockPos blockPos2,
                                  BlockState blockState, Fluid fluid, ItemStack itemStack2){
        if(fluid instanceof PotionFluid){
            boolean insert = false;

            ItemStack potionBucket = new ItemStack(FluidPotions.POTION_BUCKET);
            if (user.abilities.creativeMode) {
                if (user.inventory.contains(potionBucket)) {
                    for(int slot = 0; slot < user.inventory.size(); slot++){
                        if(user.inventory.getStack(slot).isItemEqual(potionBucket)){
                            user.inventory.removeStack(slot);
                            break;
                        }
                    }
                    insert = true;
                }
                PotionUtil.setPotion(potionBucket, ((PotionFluid) fluid).getPotion());

                if (insert) {
                    user.inventory.insertStack(potionBucket);
                }
            }else{
                PotionUtil.setPotion(itemStack2, ((PotionFluid)fluid).getPotion());
            }
        }
    }
}
