package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.complexmaterials.ComplexMaterial;
import ru.bclib.interfaces.PostInitable;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.registry.BaseBlockEntityRenders;

public class BCLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ComplexMaterial.getAllMaterials().forEach(material -> material.init());
		ModIntegrationAPI.registerAll();
		BaseBlockEntityRenders.register();
		PostInitAPI.postInit(true);
	}
}
