package ru.bclib.api.dataexchange;

import java.util.Set;

abstract class Connector {
	protected final DataExchangeAPI api;
	
	Connector(DataExchangeAPI api) {
		this.api = api;
	}
	public abstract boolean onClient();
	
	protected Set<DataHandlerDescriptor> getDescriptors(){
		return api.descriptors;
	}
}
