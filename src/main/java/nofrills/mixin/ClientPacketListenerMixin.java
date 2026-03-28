package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.chat.Component;
import nofrills.events.*;
import nofrills.features.general.NoRender;
import nofrills.features.tweaks.AnimationFix;
import nofrills.features.tweaks.NoConfirmScreen;
import nofrills.hud.HudManager;
import nofrills.misc.SkyblockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Optional;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V"))
    private void onPreTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent.equals(mc.player) && AnimationFix.active()) {
            for (SynchedEntityData.DataValue<?> entry : new ArrayList<>(packet.packedItems())) {
                if (entry.serializer().equals(EntityDataSerializers.POSE)) {
                    packet.packedItems().remove(entry);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "handleSetEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onPostTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local Entity ent) {
        if (ent instanceof LivingEntity || ent instanceof ItemEntity) {
            if (ent instanceof ArmorStand) {
                for (SynchedEntityData.DataValue<?> entry : packet.packedItems()) {
                    if (entry.serializer().equals(EntityDataSerializers.OPTIONAL_COMPONENT) && entry.value() != null) {
                        ((Optional<Component>) entry.value()).ifPresent(value -> eventBus.post(new EntityNamedEvent(ent, value)));
                        break;
                    }
                }
            }
            eventBus.post(new EntityUpdatedEvent(ent));
        }
    }

    @Inject(method = "handleAddEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;postAddEntitySoundInstance(Lnet/minecraft/world/entity/Entity;)V"))
    private void onEntitySpawn(ClientboundAddEntityPacket packet, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityUpdatedEvent(ent));
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onUpdateInventory(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (mc.screen instanceof ContainerScreen container) {
            eventBus.post(new SlotUpdateEvent(packet, container, container.getMenu(), packet.getSlot()));
        } else if (mc.screen == null) {
            eventBus.post(new InventoryUpdateEvent(packet, packet.getItem(), packet.getSlot()));
        }
    }

    @Inject(method = "handleSoundEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onPlaySound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (eventBus.post(new PlaySoundEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleParticleEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onParticle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (eventBus.post(new SpawnParticleEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "sendUnattendedCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;verifyCommand(Ljava/lang/String;)Lnet/minecraft/client/multiplayer/ClientPacketListener$CommandCheckResult;"))
    private ClientPacketListener.CommandCheckResult onParseCommand(ClientPacketListener.CommandCheckResult original) {
        if (NoConfirmScreen.instance.isActive()) {
            return ClientPacketListener.CommandCheckResult.NO_ISSUES;
        }
        return original;
    }

    @WrapWithCondition(method = "handleTakeItemEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"))
    private boolean onAddPickupParticle(ParticleEngine instance, Particle particle, @Local Entity entity) {
        return !(NoRender.instance.isActive() && NoRender.expOrbs.value() && entity instanceof ExperienceOrb);
    }

    @Inject(method = "handleAddObjective", at = @At("TAIL"))
    private void onObjectiveUpdate(ClientboundSetObjectivePacket packet, CallbackInfo ci) {
        SkyblockData.updateObjective();
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At("TAIL"))
    private void onScoreUpdate(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        SkyblockData.markScoreboardDirty();
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void onTabListUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        SkyblockData.markTabListDirty();
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onJoinGame(ClientboundLoginPacket packet, CallbackInfo ci) {
        eventBus.post(new ServerJoinEvent());
    }

    @Inject(method = "handleMapItemData", at = @At("TAIL"))
    private void onAfterMapUpdate(ClientboundMapItemDataPacket packet, CallbackInfo ci) {
        if (HudManager.dungeonMap.isActive()) {
            HudManager.dungeonMap.onMapUpdate(packet);
        }
    }
}