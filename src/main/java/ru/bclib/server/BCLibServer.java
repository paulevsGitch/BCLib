package ru.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.Registry;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;
import ru.bclib.complexmaterials.ComplexMaterial;
import ru.bclib.interfaces.PostInitable;

public class BCLibServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ComplexMaterial.getAllMaterials().forEach(material -> material.init());
		ModIntegrationAPI.registerAll();
		PostInitAPI.postInit(false);
	}
}
