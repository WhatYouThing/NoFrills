package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.EntityNamedEvent;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ScreenUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V"))
    private void onTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (Config.sneakFix && ent == mc.player) {
            packet.trackedValues().removeIf(entry -> entry.handler().equals(TrackedDataHandlerRegistry.ENTITY_POSE));
        }
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
            if (entry.handler() == TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) {
                if (entry.value() != null && ent.getCustomName() != null) {
                    eventBus.post(new EntityNamedEvent(ent, Formatting.strip(ent.getCustomName().getString())));
                    break;
                }
            }
        }
    }

    @Inject(method = "onInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;updateSlotStacks(ILjava/util/List;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void onUpdateInventory(InventoryS2CPacket packet, CallbackInfo ci, @Local PlayerEntity entity) {
        eventBus.post(new ScreenUpdateEvent(packet, mc.currentScreen));
    }

    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V"), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new PlaySoundEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}