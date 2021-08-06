package ru.bclib.api.dataexchange;

import ru.bclib.api.dataexchange.handler.DataExchange;

import java.util.Set;

abstract class Connector {
	protected final DataExchange api;
	
	Connector(DataExchange api) {
		this.api = api;
	}
	public abstract boolean onClient();
	
	protected Set<DataHandlerDescriptor> getDescriptors(){
		return api.getDescriptors();
	}
}
