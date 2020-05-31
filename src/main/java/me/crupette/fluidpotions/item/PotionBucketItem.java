package me.crupette.fluidpotions.item;

import me.crupette.fluidpotions.FluidPotions;
import me.crupette.fluidpotions.fluid.PotionFluid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.List;

public class PotionBucketItem extends Item {
    public PotionBucketItem(Settings settings) {
        super(settings);
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getStackForRender() {
        return PotionUtil.setPotion(super.getStackForRender(), Potions.HEALING);
    }

    public static PotionFluid getPotionFluid(ItemStack stack){
        return FluidPotions.INSTANCE.getStill(PotionUtil.getPotion(stack));
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        PotionFluid fluid = FluidPotions.INSTANCE.getStill(PotionUtil.getPotion(itemStack));
        BlockHitResult hitResult = rayTrace(world, user, RayTraceContext.FluidHandling.NONE);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        } else {
            BlockHitResult blockHitResult = hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (world.canPlayerModifyAt(user, blockPos) && user.canPlaceOn(blockPos2, direction, itemStack)) {
                if (this.placeFluid(fluid, user, world, blockPos2, blockHitResult)) {
                    this.onEmptied(world, itemStack, blockPos2);
                    if (user instanceof ServerPlayerEntity) {
                        Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos2, itemStack);
                    }

                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.method_29237(this.getEmptiedStack(itemStack, user), world.isClient());
                }
            } else {
                return TypedActionResult.fail(itemStack);
            }
        }
        return TypedActionResult.fail(itemStack);
    }

    protected ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        return !player.abilities.creativeMode ? new ItemStack(Items.BUCKET) : stack;
    }

    public void onEmptied(World world, ItemStack stack, BlockPos pos) {
    }


    public boolean placeFluid(Fluid fluid, PlayerEntity player, World world, BlockPos pos, BlockHitResult blockHitResult) {
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            Material material = blockState.getMaterial();
            boolean bl = blockState.canBucketPlace(fluid);
            boolean bl2 = blockState.isAir() || bl || block instanceof FluidFillable && ((FluidFillable)block).canFillWithFluid(world, pos, blockState, fluid);
            if (!bl2) {
                return blockHitResult != null && this.placeFluid(fluid, player, world, blockHitResult.getBlockPos().offset(blockHitResult.getSide()), (BlockHitResult)null);
            } else {
                if (!world.isClient && bl && !material.isLiquid()) {
                    world.breakBlock(pos, true);
                }

                if (!world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11) && !blockState.getFluidState().isStill()) {
                    return false;
                } else {
                    this.playEmptyingSound(player, world, pos);
                    return true;
                }
            }
    }

    public String getTranslationKey(ItemStack stack) {
        return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        PotionUtil.buildTooltip(stack, tooltip, 1.0F);
    }

    protected void playEmptyingSound(PlayerEntity player, WorldAccess world, BlockPos pos) {
        SoundEvent soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(player, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {

            for (Potion potion : FluidPotions.INSTANCE.getRegisteredPotions()) {
                if (potion != Potions.EMPTY) {
                    stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                }
            }
        }

    }
}
