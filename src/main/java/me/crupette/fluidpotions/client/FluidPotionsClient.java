package me.crupette.fluidpotions.client;

import me.crupette.fluidpotions.FluidPotions;
import me.crupette.fluidpotions.fluid.PotionFluid;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class FluidPotionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ResourceReloadListener());
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
            for(Potion potion : FluidPotions.INSTANCE.getRegisteredPotions()){
                FluidRenderHandler potionFluidRenderHandler = new FluidRenderHandler() {
                    @Override
                    public Sprite[] getFluidSprites(BlockRenderView blockRenderView, BlockPos blockPos, FluidState fluidState) {
                        return new Sprite[] {
                                MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(WATER_STILL_SPRITE_ID),
                                MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(WATER_FLOWING_SPRITE_ID)};
                    }

                    @Override
                    public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state){
                        return PotionUtil.getColor(potion);
                    }


                };



                FluidRenderHandlerRegistry.INSTANCE.register(FluidPotions.INSTANCE.getStill(potion), potionFluidRenderHandler);
                FluidRenderHandlerRegistry.INSTANCE.register(FluidPotions.INSTANCE.getFlowing(potion), potionFluidRenderHandler);
            }
        }
    }
}
