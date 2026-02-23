package nofrills.mixin;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import nofrills.events.ServerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {

    @Inject(method = "onPing", at = @At("TAIL"))
    private void onServerTick(CommonPingS2CPacket packet, CallbackInfo ci) {
        if (packet.getParameter() != 0) {
            eventBus.post(new ServerTickEvent());
        }
    }
}
