package org.betterx.bclib.mixin.common;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;

import com.google.common.collect.Maps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;


@Mixin(PresetEditor.class)
interface PresetEditorMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;"))
    private static <K, V> Map<K, V> bcl_foo(K k1, V v1, K k2, V v2) {
        Map<K, V> a = Maps.newHashMap();
        a.put(k1, v1);
        a.put(k2, v2);
        return a;
    }

}