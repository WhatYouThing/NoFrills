package nofrills.mixin;

import net.minecraft.world.entity.Entity;
import nofrills.events.EntityRemovedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void onBeforeRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        eventBus.post(new EntityRemovedEvent((Entity) (Object) this, reason));
    }
}
