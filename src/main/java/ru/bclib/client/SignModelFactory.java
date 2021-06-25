package ru.bclib.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApiStatus.Internal
public class SignModelFactory {
    public static final Set<WoodType> TYPES = new HashSet<>();

    public final Map<WoodType, SignRenderer.SignModel> signModels;
    public final SignRenderer.SignModel defaultModel;

    public SignModelFactory(EntityModelSet ctx) {
        //build a list of all new sign models.
        this.signModels = (Map) TYPES.stream().collect(ImmutableMap.toImmutableMap((signType) -> {
            return signType;
        }, (signType) -> {
            return new SignRenderer.SignModel(ctx.bakeLayer(ModelLayers.createSignModelName(signType)));
        }));

        //set up a default model
        defaultModel = new SignRenderer.SignModel(ctx.bakeLayer(ModelLayers.createSignModelName(WoodType.OAK)));
    }

    public static WoodType getSignType(Block block) {
        WoodType signType2;
        if (block instanceof SignBlock) {
            signType2 = ((SignBlock) block).type();
        } else {
            signType2 = WoodType.OAK;
        }

        return signType2;
    }

    public SignRenderer.SignModel getSignModel(BlockState state) {
        WoodType woodType = getSignType(state.getBlock());
        SignRenderer.SignModel model = this.signModels.get(woodType);
        return model;
    }
}
