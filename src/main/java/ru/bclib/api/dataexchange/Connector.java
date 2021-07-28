package ru.bclib.api.dataexchange;

import java.util.HashSet;
import java.util.Set;

abstract class Connector {
	protected final DataExchangeAPI api;
	protected final Set<DataHandlerDescriptor> descriptors;
	
	Connector(DataExchangeAPI api) {
		this.api = api;
		descriptors = new HashSet<>();
	}
	public abstract boolean onClient();
	
	public void addDescriptor(DataHandlerDescriptor desc){
		this.descriptors.add(desc);
	}
}
