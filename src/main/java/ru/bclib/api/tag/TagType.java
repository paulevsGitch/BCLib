package ru.bclib.api.tag;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.level.biome.Biome;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TagType<T> {
    public static class RegistryBacked<T> extends Simple<T>{
        private final DefaultedRegistry<T> registry;

        RegistryBacked(DefaultedRegistry<T> registry) {
            super(
                    registry.key(),
                    TagManager.getTagDir(registry.key()),
                     (T element) -> {
                        ResourceLocation id = registry.getKey(element);
                        if (id != registry.getDefaultKey()) {
                            return id;
                        }
                        return null;
                    }
                 );
            this.registry = registry;
        }

        @Override
        public TagKey<T> makeTag(ResourceLocation id) {
            return registry
                    .getTagNames()
                    .filter(tagKey -> tagKey.location().equals(id))
                    .findAny()
                    .orElse(TagKey.create(registry.key(), id));
        }
    }

    public static class Simple<T> extends TagType<T> {
        Simple(ResourceKey<? extends Registry<T>> registry,
               String directory,
               Function<T, ResourceLocation> locationProvider) {
            super(registry, directory, locationProvider);
        }

        public void add(TagKey<T> tagID, T... elements) {
           super.add(tagID, elements);
        }

        public void add(T element, TagKey<T>... tagIDs) {
            super.add(element, tagIDs);
        }

        @Deprecated(forRemoval = true)
        public void add(ResourceLocation tagID, T... elements) {
            super.add(tagID, elements);
        }

        @Deprecated(forRemoval = true)
        public void add(T element, ResourceLocation... tagIDs) {
            super.add(element, tagIDs);
        }
    }

    public static class UnTyped<T> extends TagType<T> {
        UnTyped(ResourceKey<? extends Registry<T>> registry,
                        String directory) {
            super(registry, directory, (t)->{throw new RuntimeException("Using Untyped TagType with Type-Dependant access. ");});
        }
    }
    public final String directory;
    private final Map<ResourceLocation, Set<ResourceLocation>> tags = Maps.newConcurrentMap();
    public final ResourceKey<? extends Registry<T>> registryKey;
    private final Function<T, ResourceLocation> locationProvider;

    private TagType(ResourceKey<? extends Registry<T>> registry,
            String directory,
            Function<T, ResourceLocation> locationProvider) {
        this.registryKey = registry;
        this.directory = directory;
        this.locationProvider = locationProvider;
    }

    public Set<ResourceLocation> getSetForTag(ResourceLocation tagID) {
        return tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
    }

    public Set<ResourceLocation> getSetForTag(TagKey<T> tag) {
        return getSetForTag(tag.location());
    }


    /**
     * Get or create a {@link TagKey}.
     *
     * @param id - {@link ResourceLocation} of the tag;
     * @return the corresponding TagKey {@link TagKey<T>}.
     */
    public TagKey<T> makeTag(ResourceLocation id) {
        return TagKey.create(registryKey, id);
    }

    /**
     * Get or create a common {@link TagKey} (namespace is 'c').
     *
     * @param name - The name of the Tag;
     * @return the corresponding TagKey {@link TagKey<T>}.
     * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
     */
    public TagKey<T> makeCommonTag(String name) {
        return TagKey.create(registryKey, new ResourceLocation("c", name));
    }

    public void addUntyped(TagKey<T> tagID, ResourceLocation... elements) {
        Set<ResourceLocation> set = getSetForTag(tagID);
        for (ResourceLocation id : elements) {
            if (id != null) {
                set.add(id);
            }
        }
    }

    public void addUntyped(ResourceLocation element, TagKey<T>... tagIDs) {
        for (TagKey<T> tagID : tagIDs) {
            addUntyped(tagID, element);
        }
    }

    /**
     * Adds one Tag to multiple Elements.
     * @param tagID {@link TagKey< Biome >} tag ID.
     * @param elements array of Elements to add into tag.
     */
    protected void add(TagKey<T> tagID, T... elements) {
        Set<ResourceLocation> set = getSetForTag(tagID);
        for (T element : elements) {
            ResourceLocation id = locationProvider.apply(element);
            if (id != null) {
                set.add(id);
            }
        }
    }

    protected void add(T element, TagKey<T>... tagIDs) {
        for (TagKey<T> tagID : tagIDs) {
            add(tagID, element);
        }
    }

    @Deprecated(forRemoval = true)
    protected void add(ResourceLocation tagID, T... elements) {
        Set<ResourceLocation> set = getSetForTag(tagID);
        for (T element : elements) {
            ResourceLocation id = locationProvider.apply(element);
            if (id != null) {
                set.add(id);
            }
        }
    }

    @Deprecated(forRemoval = true)
    protected void add(T element, ResourceLocation... tagIDs) {
        for (ResourceLocation tagID : tagIDs) {
            add(tagID, element);
        }
    }

    public void forEach(BiConsumer<ResourceLocation, Set<ResourceLocation>> consumer) {
        tags.forEach(consumer);
    }
}
