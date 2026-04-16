package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import nofrills.events.*;
import nofrills.features.general.NoRender;
import nofrills.features.tweaks.AnimationFix;
import nofrills.features.tweaks.DisconnectFix;
import nofrills.features.tweaks.NoConfirmScreen;
import nofrills.hud.HudManager;
import nofrills.misc.SkyblockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.*;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V"))
    private void onPreTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent.equals(mc.player) && AnimationFix.active()) {
            for (DataTracker.SerializedEntry<?> entry : new ArrayList<>(packet.trackedValues())) {
                if (entry.handler().equals(TrackedDataHandlerRegistry.ENTITY_POSE)) {
                    packet.trackedValues().remove(entry);
                    break;
                }
            }
        }
    }

    @WrapOperation(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V"))
    private void onWriteTrackerUpdate(DataTracker instance, List<DataTracker.SerializedEntry<?>> entries, Operation<Void> original) {
        if (DisconnectFix.instance.isActive()) {
            try {
                original.call(instance, entries);
            } catch (Exception exception) {
                LOGGER.error("Disconnect Fix caught exception in corrupted packet.", exception);
            }
        } else {
            original.call(instance, entries);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent instanceof LivingEntity || ent instanceof ItemEntity) {
            if (ent instanceof ArmorStandEntity) {
                for (DataTracker.SerializedEntry<?> entry : packet.trackedValues()) {
                    if (entry.handler().equals(TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT) && entry.value() != null) {
                        ((Optional<Text>) entry.value()).ifPresent(value -> eventBus.post(new EntityNamedEvent(ent, value)));
                        break;
                    }
                }
            }
            eventBus.post(new EntityUpdatedEvent(ent));
        }
    }

    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;playSpawnSound(Lnet/minecraft/entity/Entity;)V"))
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityUpdatedEvent(ent));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onUpdateInventory(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            eventBus.post(new SlotUpdateEvent(packet, container, container.getScreenHandler(), packet.getSlot()));
        } else if (mc.currentScreen == null) {
            eventBus.post(new InventoryUpdateEvent(packet, packet.getStack(), packet.getSlot()));
        }
    }

    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new PlaySoundEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        if (eventBus.post(new SpawnParticleEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "runClickEventCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;parseCommand(Ljava/lang/String;)Lnet/minecraft/client/network/ClientPlayNetworkHandler$CommandRunResult;"))
    private ClientPlayNetworkHandler.CommandRunResult onParseCommand(ClientPlayNetworkHandler.CommandRunResult original) {
        if (NoConfirmScreen.instance.isActive()) {
            return ClientPlayNetworkHandler.CommandRunResult.NO_ISSUES;
        }
        return original;
    }

    @WrapOperation(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addParticle(Lnet/minecraft/client/particle/Particle;)V"))
    private void onAddPickupParticle(ParticleManager instance, Particle particle, Operation<Void> original, @Local Entity entity) {
        if (NoRender.instance.isActive() && NoRender.expOrbs.value() && entity instanceof ExperienceOrbEntity) {
            return;
        }
        original.call(instance, particle);
    }

    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("TAIL"))
    private void onObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        SkyblockData.updateObjective();
    }

    @Inject(method = "onTeam", at = @At("TAIL"))
    private void onScoreUpdate(TeamS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markScoreboardDirty();
    }

    @Inject(method = "onPlayerList", at = @At("TAIL"))
    private void onTabListUpdate(PlayerListS2CPacket packet, CallbackInfo ci) {
        SkyblockData.markTabListDirty();
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onJoinGame(GameJoinS2CPacket packet, CallbackInfo ci) {
        eventBus.post(new ServerJoinEvent());
    }

    @Inject(method = "onMapUpdate", at = @At("TAIL"))
    private void onAfterMapUpdate(MapUpdateS2CPacket packet, CallbackInfo ci) {
        if (HudManager.dungeonMap.isActive()) {
            HudManager.dungeonMap.onMapUpdate(packet);
        }
    }
}