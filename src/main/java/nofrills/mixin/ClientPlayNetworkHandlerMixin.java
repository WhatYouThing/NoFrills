package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.*;
import nofrills.misc.Utils;
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
        if (Utils.isFixEnabled(Config.sneakFix) && ent == mc.player) {
            packet.trackedValues().removeIf(entry -> entry.handler().equals(TrackedDataHandlerRegistry.ENTITY_POSE));
        }
    }

    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent instanceof ArmorStandEntity) {
            TrackedDataHandler<?> textComponent = TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT;
            for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
                if (entry.handler().equals(textComponent) && entry.value() != null && ent.getCustomName() != null) {
                    eventBus.post(new EntityNamedEvent(ent, Formatting.strip(ent.getCustomName().getString())));
                    break;
                }
            }
        } else if (ent instanceof LivingEntity) {
            eventBus.post(new EntityUpdatedEvent(ent));
        }
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onUpdateInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (mc.currentScreen instanceof GenericContainerScreen containerScreen) {
            GenericContainerScreenHandler handler = containerScreen.getScreenHandler();
            Inventory inventory = handler.getInventory();
            int slot = packet.getSlot();
            eventBus.post(new ScreenSlotUpdateEvent(packet, containerScreen, handler, inventory, slot, inventory.getStack(slot), containerScreen.getTitle().getString(), packet.getSlot() == handler.slots.getLast().id));
        }
    }

    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V"), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new PlaySoundEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new SpawnParticleEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("TAIL"))
    private void onObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ObjectiveUpdateEvent(packet));
    }

    @Inject(method = "onTeam", at = @At("TAIL"))
    private void onScoreUpdate(TeamS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ScoreboardUpdateEvent(packet));
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoinGame(GameJoinS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ServerJoinEvent());
    }
}