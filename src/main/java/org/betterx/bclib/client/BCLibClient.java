package org.betterx.bclib.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.ModIntegrationAPI;
import org.betterx.bclib.api.PostInitAPI;
import org.betterx.bclib.api.dataexchange.DataExchangeAPI;
import org.betterx.bclib.blocks.BaseStairsBlock;
import org.betterx.bclib.client.models.CustomModelBakery;
import org.betterx.bclib.client.presets.WorldPresetsUI;
import org.betterx.bclib.registry.BaseBlockEntityRenders;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.jetbrains.annotations.Nullable;

public class BCLibClient implements ClientModInitializer, ModelResourceProvider, ModelVariantProvider {
    public static CustomModelBakery modelBakery;

    @Override
    public void onInitializeClient() {
        ModIntegrationAPI.registerAll();
        BaseBlockEntityRenders.register();
        DataExchangeAPI.prepareClientside();
        PostInitAPI.postInit(true);
        modelBakery = new CustomModelBakery();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> this);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> this);

        WorldPresetsUI.setupClientside();
        //dumpDatapack();
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId,
                                                    ModelProviderContext context) throws ModelProviderException {
        return modelBakery.getBlockModel(resourceId);
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelResourceLocation modelId,
                                                   ModelProviderContext context) throws ModelProviderException {
        return modelId.getVariant().equals("inventory")
                ? modelBakery.getItemModel(modelId)
                : modelBakery.getBlockModel(modelId);
    }

    public static void dumpDatapack() {
        final RegistryAccess registryAccess = RegistryAccess.builtinCopy();
        final RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder = gsonBuilder.setPrettyPrinting();   //Sets pretty formatting
        Gson gson = gsonBuilder.create();
        registryAccess.registries().forEach(r -> dumpDatapack(r, registryOps, gson));
    }

    public static <T> void dumpDatapack(RegistryAccess.RegistryEntry<T> registry,
                                        RegistryOps<JsonElement> registryOps,
                                        Gson gson) {
        File base = new File(System.getProperty("user.dir"), "bclib_datapack_dump");
        BCLib.LOGGER.info(registry.key().toString());

        registry
                .value()
                .entrySet()
                .stream()
                .map(e -> e.getKey()).map(key -> registry.value().getHolder(key).get())
                .forEach(holder -> {
                    File f1 = new File(base, holder.unwrapKey().get().location().getNamespace());
                    f1 = new File(f1, registry.key().location().getPath());
                    f1.mkdirs();
                    f1 = new File(f1, holder.unwrapKey().get().location().getPath() + ".json");

                    Codec[] codec = {null};

                    BCLib.LOGGER.info("   - " + f1);
                    Object obj = holder;

                    while (obj instanceof Holder<?>) {
                        obj = ((Holder<?>) obj).value();
                    }

                    if (obj instanceof Structure s) {
                        codec[0] = s.type().codec();
                    } else if (obj instanceof StructureProcessorList s) {
                        codec[0] = StructureProcessorType.LIST_OBJECT_CODEC;
                    } else if (obj instanceof GameEvent) {
                        return;
                    } else if (obj instanceof Fluid) {
                        return;
                    } else if (obj instanceof MobEffect) {
                        return;
                    } else if (obj instanceof BaseStairsBlock) {
                        return;
                    }

                    if (codec[0] == null) {
                        for (Method m : obj.getClass().getMethods()) {
                            if (!Modifier.isStatic(m.getModifiers())) {
                                m.setAccessible(true);
                                if (m.getParameterTypes().length == 0) {
                                    if (Codec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = (Codec) m.invoke(obj);
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchDataCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchDataCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (codec[0] == null) {
                        //Try to find DIRECT_CODEC field
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("DIRECT_CODEC".equals(f.getName())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("      !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find CODEC field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("CODEC".equals(f.getName())) {
                                    try {
                                        f.setAccessible(true);
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find any Codec field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if (Codec.class.isAssignableFrom(f.getType())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }


                    if (codec[0] != null) {
                        try {
                            var o = codec[0]
                                    .encodeStart(registryOps, holder.value())
                                    .result()
                                    .orElse(new JsonObject());

                            String content = gson.toJson(o);
                            try {
                                Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                BCLib.LOGGER.error("      ->> Unable to WRITE: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            BCLib.LOGGER.error("      ->> Unable to encode: " + e.getMessage());
                        }
                    } else {
                        BCLib.LOGGER.error("     !!! Could not determine Codec");
                    }
                });
        ;
    }
}
