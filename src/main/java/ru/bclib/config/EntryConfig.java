package ru.bclib.config;

public class EntryConfig extends IdConfig {
	public EntryConfig(String modID, String group) {
		super(modID, group, (id, entry) -> {
			return new ConfigKey(entry, id);
		});
	}
}
