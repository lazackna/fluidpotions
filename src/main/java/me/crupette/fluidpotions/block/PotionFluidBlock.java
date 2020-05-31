package me.crupette.fluidpotions.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.potion.Potion;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PotionFluidBlock extends FluidBlock {
    private final Potion potion;
    public PotionFluidBlock(Potion potion, BaseFluid fluid, Settings settings) {
        super(fluid, settings);
        this.potion = potion;
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity)entity;
        if(potion.hasInstantEffect()){
            if(!state.getFluidState().isStill()) return;
        }
        if(!world.isClient){
            List<StatusEffectInstance> list = potion.getEffects();
            for (StatusEffectInstance statusEffectInstance : list) {
                StatusEffectInstance adjustedInstance = new StatusEffectInstance(
                        statusEffectInstance.getEffectType(),
                        statusEffectInstance.getDuration() / 10,
                        statusEffectInstance.getAmplifier());
                if(potion.hasInstantEffect()){
                    statusEffectInstance.getEffectType().applyInstantEffect(livingEntity, livingEntity, livingEntity, adjustedInstance.getAmplifier() * 3, 1.0D);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    world.updateNeighbors(pos, Blocks.AIR);
                }else {
                    livingEntity.getStatusEffects();
                    livingEntity.addStatusEffect(adjustedInstance);
                }
            }
        }
    }
}
