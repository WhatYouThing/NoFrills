package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import nofrills.events.EntityRemovedEvent;
import nofrills.events.WorldTickEvent;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(CallbackInfo ci) {
        if (mc.player != null) {
            eventBus.post(new WorldTickEvent());
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void onBeforeRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityRemovedEvent(ent, removalReason));
    }

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void onBreakParticle(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.breakParticles.value()) {
            ci.cancel();
        }
    }

    @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
    private void onBreakingParticle(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.breakParticles.value()) {
            ci.cancel();
        }
    }
}