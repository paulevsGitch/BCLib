package ru.bclib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfigUI {
	/**
	 * When {@code true}, this option will not generate UI-Elements.
	 */
	public boolean hide() default false;
	
	/**
	 * When a Widget is generated for this option, it will be indented by this Value
	 */
	public int leftPadding() default 0;
	
	/**
	 * When a Widget is generated for this option, it will be indented by this Value
	 */
	public int topPadding() default 0;
}
