package ru.bclib.api;

import java.util.List;

import com.google.common.collect.Lists;

import ru.bclib.integration.ModIntegration;

public class ModIntegrationAPI {
	private static final List<ModIntegration> INTEGRATIONS = Lists.newArrayList();
	
	/**
	 * Registers mod integration
	 * @param integration
	 * @return
	 */
	public static ModIntegration register(ModIntegration integration) {
		INTEGRATIONS.add(integration);
		if (integration.modIsInstalled()) {
			integration.init();
		}
		return integration;
	}
	
	/**
	 * Get all registered mod integrations.
	 * @return {@link List} of {@link ModIntegration}.
	 */
	public static List<ModIntegration> getIntegrations() {
		return INTEGRATIONS;
	}
}
