package ru.bclib.api.dataexchange;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class DataHandlerDescriptor {
	public DataHandlerDescriptor(ResourceLocation identifier, Supplier<DataHandler> instancer){
		this(identifier, instancer, false);
	}
	public DataHandlerDescriptor(ResourceLocation identifier, Supplier<DataHandler> instancer, boolean sendOnJoin){
		this.instancer = instancer;
		this.identifier = identifier;
		this.sendOnJoin = sendOnJoin;
	}
	
	public final boolean sendOnJoin;
	public final ResourceLocation identifier;
	public final Supplier<DataHandler> instancer;
}
