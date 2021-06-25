package ru.bclib.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bclib.util.TagHelper;

import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
	@Shadow
	private String directory;

	@ModifyArg(method = "loadAndBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;build(Ljava/util/Map;)Lnet/minecraft/tags/TagCollection;"))
	public Map<ResourceLocation, Tag.Builder> be_modifyTags(Map<ResourceLocation, Tag.Builder> tagsMap) {
		return TagHelper.apply(directory, tagsMap);
	}
}
