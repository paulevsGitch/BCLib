package ru.bclib.mixin.client;

import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLoginPacket.class)
public abstract class ClientboundLoginPacketMixin {
	@Inject(method = "handle", cancellable = true, at=@At("HEAD"))
	public void bclib_handle(ClientGamePacketListener clientGamePacketListener, CallbackInfo ci){
		//cLevel.setBCLibDidSendHello();
//		DataExchangeAPI.sendOnEnter();
//		ci.cancel();
	}
}
