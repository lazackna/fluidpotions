package me.crupette.fluidpotions.fluid;

import me.crupette.fluidpotions.FluidPotions;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;

public abstract class PotionFluidGenerated extends PotionFluid{
    protected PotionFluidGenerated(Potion potion) {
        super(potion);
    }

    @Override
    public Fluid getFlowing() {
        return FluidPotions.getFlowing(potion);
    }

    @Override
    public Fluid getStill() {
        return FluidPotions.getStill(potion);
    }

    @Override
    public Item getBucketItem() {
        return FluidPotions.getBucketItem(potion);
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return FluidPotions.getFluidBlock(potion).getDefaultState().with(Properties.LEVEL_15, method_15741(state));
    }



    public static class Still extends PotionFluidGenerated {

        public Still(Potion potion) {
            super(potion);
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

    }

    public static class Flowing extends PotionFluidGenerated {

        public Flowing(Potion potion) {
            super(potion);
        }

        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

    }

}
