package nofrills.mixin;

import net.minecraft.network.syncher.SynchedEntityData;
import nofrills.features.tweaks.DisconnectFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
    @Shadow
    @Final
    private SynchedEntityData.DataItem<?>[] itemsById;

    @Inject(method = "assignValues", at = @At("HEAD"))
    private void onWriteEntries(List<SynchedEntityData.DataValue<?>> entries, CallbackInfo ci) {
        if (DisconnectFix.active()) {
            entries.removeIf(entry -> entry.id() > this.itemsById.length - 1);
        }
    }
}