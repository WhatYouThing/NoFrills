package nofrills.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.Formatting;
import nofrills.events.EntityNamedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
    private void onTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        Entity entity = mc.world.getEntityById(packet.id());
        if (entity != null) {
            for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
                if (entry.handler() == TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) {
                    if (entry.value() != null && entity.getCustomName() != null) {
                        eventBus.post(new EntityNamedEvent(entity, Formatting.strip(entity.getCustomName().getString())));
                        break;
                    }
                }
            }
        }
    }
}