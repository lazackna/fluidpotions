package me.crupette.fluidpotions.item;

import me.crupette.fluidpotions.FluidPotions;
import me.crupette.fluidpotions.fluid.PotionFluid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
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
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

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
        return FluidPotions.getStill(PotionUtil.getPotion(stack));
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        PotionFluid potionFluid = getPotionFluid(itemStack);
        HitResult hitResult = rayTrace(world, user, RayTraceContext.FluidHandling.NONE);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        } else {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (world.canPlayerModifyAt(user, blockPos) && user.canPlaceOn(blockPos2, direction, itemStack)) {
                BlockState blockState;
                blockState = world.getBlockState(blockPos);
                BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && potionFluid == Fluids.WATER ? blockPos : blockPos2;
                if (this.placeFluid(potionFluid, user, world, blockPos3, blockHitResult)) {
                    this.onEmptied(world, itemStack, blockPos3);
                    if (user instanceof ServerPlayerEntity) {
                        Criterions.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos3, itemStack);
                    }

                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.success(this.getEmptiedStack(itemStack, user));
                } else {
                    return TypedActionResult.fail(itemStack);
                }
            }
            return TypedActionResult.fail(itemStack);
        }
    }

    protected ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        return !player.abilities.creativeMode ? new ItemStack(Items.BUCKET) : stack;
    }

    public void onEmptied(World world, ItemStack stack, BlockPos pos) {
    }

    public boolean placeFluid(Fluid potionFluid, PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult) {
        if (!(potionFluid instanceof BaseFluid)) {
            return false;
        } else {
            BlockState blockState = world.getBlockState(pos);
            Material material = blockState.getMaterial();
            boolean bl = blockState.canBucketPlace(potionFluid);
            if (!blockState.isAir() && !bl && (!(blockState.getBlock() instanceof FluidFillable) || !((FluidFillable)blockState.getBlock()).canFillWithFluid(world, pos, blockState, potionFluid))) {
                return hitResult != null && this.placeFluid(potionFluid, player, world, hitResult.getBlockPos().offset(hitResult.getSide()), (BlockHitResult) null);
            } else if (world.getDimension().isNether() && potionFluid.matches(FluidTags.WATER)) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                for(int l = 0; l < 8; ++l) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
                }

                return true;
            } else {
                if (!world.isClient && bl && !material.isLiquid()) {
                    world.breakBlock(pos, true);
                }

                this.playEmptyingSound(player, world, pos);
                world.setBlockState(pos, potionFluid.getDefaultState().getBlockState(), 11);
                return true;
            }
        }
    }

    protected void playEmptyingSound(PlayerEntity player, IWorld world, BlockPos pos) {
        SoundEvent soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(player, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public String getTranslationKey(ItemStack stack) {
        return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        PotionUtil.buildTooltip(stack, tooltip, 1.0F);
    }

    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {

            for(Potion potion : FluidPotions.getRegisteredPotions()) {
                if (potion != Potions.EMPTY) {
                    stacks.add(PotionUtil.setPotion(new ItemStack(this), potion));
                }
            }
        }

    }
}
