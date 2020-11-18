package me.crupette.fluidpotions;

import com.google.common.collect.ImmutableList;
import me.crupette.fluidpotions.block.PotionFluidBlock;
import me.crupette.fluidpotions.fluid.PotionFluid;
import me.crupette.fluidpotions.fluid.PotionFluidGenerated;
import me.crupette.fluidpotions.item.PotionBucketItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.FluidTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidPotions implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "fluidpotions";
    public static final String MOD_NAME = "Fluid Potions";

    private static final Map<Identifier, PotionFluid>       stillFluids = new HashMap<>();
    private static final Map<Identifier, PotionFluid>       flowingFluids = new HashMap<>();
    private static final Map<Identifier, PotionBucketItem>  bucketItems = new HashMap<>();
    private static final Map<Identifier, PotionFluidBlock>  fluidBlocks = new HashMap<>();
    private static final List<Potion>                       registeredPotions = new ArrayList<>();

    @Deprecated
    public static FluidPotions      INSTANCE = null;
    public static PotionBucketItem  POTION_BUCKET = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "potion_bucket"), new PotionBucketItem(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(ItemGroup.BREWING)));

    @Override
    public void onInitialize() {
        Registry.POTION.forEach(potion -> {
            registerPotion(Registry.POTION.getId(potion), potion);
        });
        RegistryEntryAddedCallback.event(Registry.POTION).register((i, identifier, potion) -> {
            System.out.println("Callback registration for potion " + identifier);
            FluidPotions.registerPotion(identifier, potion);
        });
    }

    public static void registerPotion(Identifier id, Potion potion){
        if(Registry.POTION.getId(potion).compareTo(new Identifier("water")) == 0) return;
        stillFluids.put(id, Registry.register(
                Registry.FLUID, new Identifier(MOD_ID, id.getPath()), new PotionFluidGenerated.Still(potion)));
        flowingFluids.put(id, Registry.register(
                Registry.FLUID, new Identifier(MOD_ID, id.getPath() + "_flowing"), new PotionFluidGenerated.Flowing(potion)));
        bucketItems.put(id, POTION_BUCKET);
        fluidBlocks.put(id, Registry.register(
                Registry.BLOCK, new Identifier(MOD_ID, id.getPath()), new PotionFluidBlock(potion, getStill(potion), FabricBlockSettings.copy(Blocks.WATER))));
        registeredPotions.add(potion);
        LOGGER.info("Added potion fluids for " + id);
    }

    public static PotionFluid getStill(Potion potion){
        return stillFluids.get(Registry.POTION.getId(potion));
    }

    public static PotionFluid getFlowing(Potion potion){
        return flowingFluids.get(Registry.POTION.getId(potion));
    }

    public static PotionBucketItem getBucketItem(Potion potion){
        return bucketItems.get(Registry.POTION.getId(potion));
    }

    public static PotionFluidBlock getFluidBlock(Potion potion){
        return fluidBlocks.get(Registry.POTION.getId(potion));
    }

    public static ImmutableList<Potion> getRegisteredPotions(){
        return ImmutableList.copyOf(registeredPotions);
    }
}