package me.crupette.fluidpotions.client;

import com.swordglowsblue.artifice.api.Artifice;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import me.crupette.fluidpotions.FluidPotions;
import me.crupette.fluidpotions.block.PotionFluidBlock;
import me.crupette.fluidpotions.fluid.PotionFluid;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FluidPotionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ResourceReloadListener());

        ArtificeResourcePack resourcePack = Artifice.registerAssets(FluidPotions.MOD_ID, pack -> {
            pack.setDisplayName(FluidPotions.MOD_NAME);
            pack.setDescription("Holds the block descriptions for all the fluid potions");

            FluidPotions.getRegisteredPotions().forEach(potion -> {
                PotionFluidBlock potionBlock = FluidPotions.getFluidBlock(potion);
                Identifier potionId = Registry.BLOCK.getId(potionBlock);

                pack.addBlockState(potionId, state -> {
                    state.variant("", settings -> {
                        settings.model(new Identifier("block/water"));
                    });
                });
            });
        });
    }

    public static class ResourceReloadListener implements SimpleSynchronousResourceReloadListener {
        private static final Identifier WATER_STILL_SPRITE_ID = new Identifier("minecraft", "block/water_still");
        private static final Identifier WATER_FLOWING_SPRITE_ID = new Identifier("minecraft", "block/water_flow");

        @Override
        public Identifier getFabricId() {
            return new Identifier(FluidPotions.MOD_ID, "fluid_resource_loader");
        }

        @Override
        public Collection<Identifier> getFabricDependencies() {
            return Arrays.asList(ResourceReloadListenerKeys.MODELS, ResourceReloadListenerKeys.TEXTURES);
        }

        @Override
        public void apply(ResourceManager manager) {
            for(Potion potion : FluidPotions.getRegisteredPotions()){
                FluidRenderHandler potionFluidRenderHandler = new FluidRenderHandler() {
                    @Override
                    public Sprite[] getFluidSprites(BlockRenderView blockRenderView, BlockPos blockPos, FluidState fluidState) {
                        return new Sprite[] {
                                MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(WATER_STILL_SPRITE_ID),
                                MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(WATER_FLOWING_SPRITE_ID)};
                    }

                    @Override
                    public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state){
                        return PotionUtil.getColor(potion);
                    }


                };

                FluidRenderHandlerRegistry.INSTANCE.register(FluidPotions.getStill(potion), potionFluidRenderHandler);
                FluidRenderHandlerRegistry.INSTANCE.register(FluidPotions.getFlowing(potion), potionFluidRenderHandler);
            }
        }
    }
}
