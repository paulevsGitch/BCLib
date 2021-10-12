package ru.bclib.mixin.client;

import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.interfaces.IStructureFeatures;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(StructureFeatures.class)
public abstract class StructureFeaturesMixin implements IStructureFeatures {
    @Inject(method="registerStructures", at=@At("TAIL"))
    private static void bclib_registerStructures(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> biConsumer, CallbackInfo ci){
        bclib_callbacks.forEach(consumer -> consumer.accept(biConsumer));
        bclib_callbacks.clear();
    }

    private static List<Consumer<BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> > bclib_callbacks = new LinkedList<>();
    public void bclib_registerStructure(Consumer<BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> callback){
        bclib_callbacks.add(callback);
   }
}
