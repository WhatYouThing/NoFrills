package nofrills.mixin;

import net.minecraft.entity.data.DataTracked;
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
    @Shadow
    @Final
    private DataTracked trackedEntity;

    @Shadow
    protected abstract <T> void copyToFrom(DataTracker.Entry<T> to, DataTracker.SerializedEntry<?> from);

    @Inject(method = "writeUpdatedEntries", at = @At("HEAD"), cancellable = true)
    private void onWriteEntries(List<DataTracker.SerializedEntry<?>> entries, CallbackInfo ci) {
        if (DisconnectFix.instance.isActive()) {
            for (DataTracker.SerializedEntry<?> serializedEntry : entries) {
                if (serializedEntry.id() > this.entries.length - 1) {
                    continue;
                }
                DataTracker.Entry<?> entry = this.entries[serializedEntry.id()];
                if (serializedEntry.value().getClass() == entry.get().getClass()) {
                    this.copyToFrom(entry, serializedEntry);
                    this.trackedEntity.onTrackedDataSet(entry.getData());
                }
            }
            this.trackedEntity.onDataTrackerUpdate(entries);
            ci.cancel();
        }
    }
}