package nofrills.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import nofrills.events.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        eventBus.post(new AttackEntityEvent(target));
    }
}
