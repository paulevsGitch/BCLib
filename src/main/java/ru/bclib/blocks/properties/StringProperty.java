package ru.bclib.blocks.properties;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

public class StringProperty extends Property<String> {

	private final Set<String> values;

	public static StringProperty create(String name, String... values) {
		return new StringProperty(name, values);
	}

	protected StringProperty(String string, String... values) {
		super(string, String.class);
		this.values = Sets.newHashSet(values);
	}

	@Override
	public Collection<String> getPossibleValues() {
		return Collections.unmodifiableSet(values);
	}

	@Override
	public String getName(String comparable) {
		return comparable;
	}

	@Override
	public Optional<String> getValue(String string) {
		if (values.contains(string)) {
			return Optional.of(string);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public int generateHashCode() {
		return super.generateHashCode() + Objects.hashCode(values);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StringProperty that)) return false;
		if (!super.equals(o)) return false;
		return values.equals(that.values);
	}
}
