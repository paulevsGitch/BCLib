package ru.bclib.mixin.common;

import java.util.Map;

import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.util.TagHelper;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
	@Shadow
	private String directory;

	//@ModifyArg(method = "prepare", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	@Inject(method="load", at=@At(value="RETURN"))
	public void be_modifyTags(ResourceManager resourceManager, CallbackInfoReturnable<Map<ResourceLocation, Tag.Builder>> cir) {
		TagHelper.apply(directory, cir.getReturnValue());
	}
}
