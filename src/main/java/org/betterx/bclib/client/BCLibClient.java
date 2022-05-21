package org.betterx.bclib.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.*;

import org.betterx.bclib.api.ModIntegrationAPI;
import org.betterx.bclib.api.PostInitAPI;
import org.betterx.bclib.api.dataexchange.DataExchangeAPI;
import org.betterx.bclib.client.models.CustomModelBakery;
import org.betterx.bclib.client.presets.WorldPresetsUI;
import org.betterx.bclib.registry.BaseBlockEntityRenders;

import org.jetbrains.annotations.Nullable;

public class BCLibClient implements ClientModInitializer, ModelResourceProvider, ModelVariantProvider {
    public static CustomModelBakery modelBakery;

    @Override
    public void onInitializeClient() {
        ModIntegrationAPI.registerAll();
        BaseBlockEntityRenders.register();
        DataExchangeAPI.prepareClientside();
        PostInitAPI.postInit(true);
        modelBakery = new CustomModelBakery();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> this);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> this);

        WorldPresetsUI.setupClientside();
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId,
                                                    ModelProviderContext context) throws ModelProviderException {
        return modelBakery.getBlockModel(resourceId);
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelResourceLocation modelId,
                                                   ModelProviderContext context) throws ModelProviderException {
        return modelId.getVariant().equals("inventory")
                ? modelBakery.getItemModel(modelId)
                : modelBakery.getBlockModel(modelId);
    }
}
