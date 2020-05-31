package me.crupette.fluidpotions;

import com.google.common.collect.ImmutableList;
import me.crupette.fluidpotions.block.PotionFluidBlock;
import me.crupette.fluidpotions.fluid.PotionFluid;
import me.crupette.fluidpotions.fluid.PotionFluidGenerated;
import me.crupette.fluidpotions.item.PotionBucketItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FluidPotions implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "fluidpotions";
    public static final String MOD_NAME = "Fluid Potions";

    private Map<Identifier, PotionFluid> stillFluids = new HashMap<>();
    private Map<Identifier, PotionFluid> flowingFluids = new HashMap<>();
    private Map<Identifier, PotionBucketItem> bucketItems = new HashMap<>();
    private Map<Identifier, PotionFluidBlock> fluidBlocks = new HashMap<>();
    private List<Potion> registeredPotions = new ArrayList<>();

    public static FluidPotions INSTANCE = null;
    public static PotionBucketItem POTION_BUCKET = new PotionBucketItem(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(ItemGroup.BREWING));

    @Override
    public void onInitialize() {
        INSTANCE = this;

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "potion_bucket"), POTION_BUCKET);

        for(Potion potion : Registry.POTION.stream().collect(Collectors.toSet())){
            if(potion.equals(Potions.WATER)) continue;
            Identifier potionId = Registry.POTION.getId(potion);
            stillFluids.put(potionId, Registry.register(
                    Registry.FLUID, new Identifier(MOD_ID, potionId.getPath()), new PotionFluidGenerated.Still(potion)));
            flowingFluids.put(potionId, Registry.register(
                    Registry.FLUID, new Identifier(MOD_ID, potionId.getPath() + "_flowing"), new PotionFluidGenerated.Flowing(potion)));
            bucketItems.put(potionId, POTION_BUCKET);
            fluidBlocks.put(potionId, Registry.register(
                    Registry.BLOCK, new Identifier(MOD_ID, potionId.getPath()), new PotionFluidBlock(potion, getStill(potion), FabricBlockSettings.copy(Blocks.WATER).build())));
            registeredPotions.add(potion);
        }
    }

    public PotionFluid getStill(Potion potion){
        return stillFluids.get(Registry.POTION.getId(potion));
    }

    public PotionFluid getFlowing(Potion potion){
        return flowingFluids.get(Registry.POTION.getId(potion));
    }

    public PotionBucketItem getBucketItem(Potion potion){
        return bucketItems.get(Registry.POTION.getId(potion));
    }

    public PotionFluidBlock getFluidBlock(Potion potion){
        return fluidBlocks.get(Registry.POTION.getId(potion));
    }

    public ImmutableList<Potion> getRegisteredPotions(){
        return ImmutableList.copyOf(this.registeredPotions);
    }
}