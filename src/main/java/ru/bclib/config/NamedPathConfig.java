package ru.bclib.config;

import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.config.NamedPathConfig.ConfigToken.Bool;
import ru.bclib.config.NamedPathConfig.ConfigToken.Float;
import ru.bclib.config.NamedPathConfig.ConfigToken.Int;
import ru.bclib.config.NamedPathConfig.ConfigToken.Str;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NamedPathConfig extends PathConfig{
	public abstract static class ConfigToken <T> extends ConfigKey{
		public static class Int extends ConfigToken<Integer>{
			public Int(int def, String entry, ResourceLocation path) { this(def, entry, path.getNamespace(), path.getPath());}
			public Int(int def, String entry, String... path) { super(def, entry, path);}
			
		}
		
		public static class Float extends ConfigToken<java.lang.Float>{
			public Float(float def, String entry, ResourceLocation path) { this(def, entry, path.getNamespace(), path.getPath());}
			public Float(float def, String entry, String... path) { super(def, entry, path); }
		}
		
		public static class Bool extends ConfigToken<Boolean>{
			public Bool(boolean def, String entry, ResourceLocation path) { this(def, entry, path.getNamespace(), path.getPath());}
			public Bool(boolean def, String entry, String... path) { super(def, entry, path); }
		}
		
		public static class Str extends ConfigToken<String>{
			public Str(String def, String entry, ResourceLocation path) { this(def, entry, path.getNamespace(), path.getPath());}
			public Str(String def, String entry, String... path) { super(def, entry, path); }
		}
		
		public static final Predicate<NamedPathConfig> ALWAYS_ENABLED = (config) -> true;
		
		public final T defaultValue;
		protected final Predicate<NamedPathConfig> enabled;
		
		ConfigToken(T defaultValue, String entry, ResourceLocation path) { this(defaultValue, entry, path.getNamespace(), path.getPath()); }
		ConfigToken(T defaultValue, String entry, String... path) { this(defaultValue, ALWAYS_ENABLED, entry, path); }
		
		ConfigToken(T defaultValue, String entry, ResourceLocation path, Predicate<NamedPathConfig> enabled) { this(defaultValue, enabled, entry, path.getNamespace(), path.getPath()); }
		ConfigToken(T defaultValue, String entry, String path, Predicate<NamedPathConfig> enabled) { this(defaultValue, enabled, entry, path); }
		ConfigToken(T defaultValue, String entry, String[] path, Predicate<NamedPathConfig> enabled) { this(defaultValue, enabled, entry, path); }
		private ConfigToken(T defaultValue, Predicate<NamedPathConfig> enabled, String entry, String... path) {
			super(entry, path);
			this.enabled = enabled;
			this.defaultValue = defaultValue
		}
		
	}
	
	public NamedPathConfig(String modID, String group, boolean autoSync, boolean diffContent) {
		super(modID, group, autoSync, diffContent);
		onInit();
	}
	
	public NamedPathConfig(String modID, String group, boolean autoSync) {
		super(modID, group, autoSync);
		onInit();
	}
	
	public NamedPathConfig(String modID, String group) {
		super(modID, group);
		onInit();
	}
	
	public List<ConfigToken> getAllOptions(){
		List<ConfigToken> res = new LinkedList<>();
		for (Field fl : this.getClass().getDeclaredFields()){
			int modifiers = fl.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && ConfigToken.class.isAssignableFrom(fl.getType())) {
				try {
					res.add((ConfigToken) fl.get(null));
				}
				catch (IllegalAccessException e) {
					BCLib.LOGGER.error("Could not access " + fl);
				}
			}
		}
		return res;
	}
	
	protected void onInit(){
		getAllOptions().forEach(e -> get(e));
		this.saveChanges();
	}
	
	private void set(ConfigToken what, Object value) {
		if (what instanceof Bool) set(what, (boolean)value);
		else if (what instanceof Int) set(what, (int)value);
		else if (what instanceof Float) set(what, (float)value);
		else if (what instanceof Str) set(what, (String)value);
		else BCLib.LOGGER.error("Accessing " + what + " as general type is not supported.");
	}
	
	private Object get(ConfigToken what){
		if (what instanceof Bool) return get((Bool)what);
		else if (what instanceof Int) return get((Int)what);
		else if (what instanceof Float) return get((Float)what);
		else if (what instanceof Str) return get((Str)what);
		else return null;
	}
	
	public void set(ConfigToken.Int what, int value) {
		this.setInt(what, value);
	}
	
	public int get(ConfigToken.Int what){
		return this.getInt(what, what.defaultValue);
	}
	
	public void set(ConfigToken.Bool what, boolean value) {
		this.setBoolean(what, value);
	}
	
	public boolean get(ConfigToken.Bool what){
		return this.getBoolean(what, what.defaultValue);
	}
	
	public void set(ConfigToken.Str what, String value) {
		this.setString(what, value);
	}
	
	public String get(ConfigToken.Str what){
		return this.getString(what, what.defaultValue);
	}
	
	public void set(ConfigToken.Float what, float value) {
		this.setFloat(what, value);
	}
	
	public float get(ConfigToken.Float what){
		return this.getFloat(what, what.defaultValue);
	}
}
