package nofrills.mixin;

import net.minecraft.entity.data.DataTracker;
import nofrills.features.tweaks.DisconnectFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DataTracker.class)
public abstract class DataTrackerMixin {
    @Shadow
    @Final
    private DataTracker.Entry<?>[] entries;

    @Inject(method = "writeUpdatedEntries", at = @At("HEAD"))
    private void onWriteEntries(List<DataTracker.SerializedEntry<?>> entries, CallbackInfo ci) {
        if (DisconnectFix.active()) {
            entries.removeIf(entry -> entry.id() > this.entries.length - 1);
        }
    }
}