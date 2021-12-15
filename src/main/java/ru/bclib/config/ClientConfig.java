package ru.bclib.config;

import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync;

public class ClientConfig extends NamedPathConfig {
	public static final ConfigToken<Boolean> ENABLED = ConfigToken.Boolean(true, "enabled", AutoSync.SYNC_CATEGORY);
	@ConfigUI(leftPadding = 8)
	public static final DependendConfigToken<Boolean> ACCEPT_CONFIGS = DependendConfigToken.Boolean(true, "acceptConfigs", AutoSync.SYNC_CATEGORY, (config) -> config.get(ENABLED));
	@ConfigUI(leftPadding = 8)
	public static final DependendConfigToken<Boolean> ACCEPT_FILES = DependendConfigToken.Boolean(true, "acceptFiles", AutoSync.SYNC_CATEGORY, (config) -> config.get(ENABLED));
	@ConfigUI(leftPadding = 8)
	public static final DependendConfigToken<Boolean> ACCEPT_MODS = DependendConfigToken.Boolean(false, "acceptMods", AutoSync.SYNC_CATEGORY, (config) -> config.get(ENABLED));
	@ConfigUI(leftPadding = 8)
	public static final DependendConfigToken<Boolean> DISPLAY_MOD_INFO = DependendConfigToken.Boolean(true, "displayModInfo", AutoSync.SYNC_CATEGORY, (config) -> config.get(ENABLED));

	@ConfigUI(topPadding = 12)
	public static final ConfigToken<Boolean> DEBUG_HASHES = ConfigToken.Boolean(false, "debugHashes", AutoSync.SYNC_CATEGORY);


	public ClientConfig() {
		super(BCLib.MOD_ID, "client", false);
	}

	public boolean shouldPrintDebugHashes() {
		return get(DEBUG_HASHES);
	}

	public boolean isAllowingAutoSync() {
		return get(ENABLED);
	}

	public boolean isAcceptingMods() {
		return get(ACCEPT_MODS) /*&& isAllowingAutoSync()*/;
	}

	public boolean isAcceptingConfigs() {
		return get(ACCEPT_CONFIGS) /*&& isAllowingAutoSync()*/;
	}

	public boolean isAcceptingFiles() {
		return get(ACCEPT_FILES) /*&& isAllowingAutoSync()*/;
	}

	public boolean isShowingModInfo() {
		return get(DISPLAY_MOD_INFO) /*&& isAllowingAutoSync()*/;
	}
}
