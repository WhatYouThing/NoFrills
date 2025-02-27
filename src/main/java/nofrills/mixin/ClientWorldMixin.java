package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import nofrills.config.Config;
import nofrills.events.BlockUpdateEvent;
import nofrills.events.EntityRemoveEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Redirect(method = "processPendingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;collidesWithStateAtPos(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private boolean doesCollide(PlayerEntity instance, BlockPos blockPos, BlockState blockState) {
        if (Utils.isFixEnabled(Config.stonkFix)) {
            return false;
        }
        return instance.collidesWithStateAtPos(blockPos, blockState);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onWorldTick(CallbackInfo ci) {
        if (mc.player == null) {
            return;
        }
        eventBus.post(new WorldTickEvent());
    }

    @Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z"))
    private void onUpdateBlock(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        eventBus.post(new BlockUpdateEvent(pos, getBlockState(pos), state));
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V"))
    private void onTrackerUpdate(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityRemoveEvent(ent, removalReason));
    }
}