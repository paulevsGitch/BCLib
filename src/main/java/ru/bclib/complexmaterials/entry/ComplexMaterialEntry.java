package ru.bclib.complexmaterials.entry;

import net.minecraft.resources.ResourceLocation;

public abstract class ComplexMaterialEntry {
	private final String suffix;
	
	protected ComplexMaterialEntry(String suffix) {
		this.suffix = suffix;
	}
	
	public String getName(String baseName) {
		return baseName + "_" + suffix;
	}
	
	public ResourceLocation getLocation(String modID, String baseName) {
		return new ResourceLocation(modID, getName(baseName));
	}
}
