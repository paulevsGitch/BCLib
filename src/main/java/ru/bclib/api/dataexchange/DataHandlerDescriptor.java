package ru.bclib.api.dataexchange;

import net.minecraft.resources.ResourceLocation;
import ru.bclib.api.dataexchange.handler.HelloClient;
import ru.bclib.api.dataexchange.handler.HelloServer;
import ru.bclib.api.dataexchange.handler.RequestFiles;
import ru.bclib.api.dataexchange.handler.SendFiles;

import java.util.function.Supplier;

public class DataHandlerDescriptor {
	public DataHandlerDescriptor(ResourceLocation identifier, Supplier<DataHandler> instancer){
		this(identifier, instancer, instancer, false, false);
	}
	
	public DataHandlerDescriptor(ResourceLocation identifier, Supplier<DataHandler> instancer, boolean sendOnJoin, boolean sendBeforeEnter){
		this(identifier, instancer, instancer, sendOnJoin, sendBeforeEnter);
	}
	public DataHandlerDescriptor(ResourceLocation identifier, Supplier<DataHandler> receiv_instancer, Supplier<DataHandler> join_instancer, boolean sendOnJoin, boolean sendBeforeEnter){
		this.INSTANCE = receiv_instancer;
		this.JOIN_INSTANCE = join_instancer;
		this.IDENTIFIER = identifier;
		
		this.sendOnJoin = sendOnJoin;
		this.sendBeforeEnter = sendBeforeEnter;
	}
	
	public final boolean sendOnJoin;
	public final boolean sendBeforeEnter;
	public final ResourceLocation IDENTIFIER;
	public final Supplier<DataHandler> INSTANCE;
	public final Supplier<DataHandler> JOIN_INSTANCE;
}
