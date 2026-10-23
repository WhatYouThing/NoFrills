package nofrills.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import nofrills.events.AttackEntityEvent;
import nofrills.hud.HudManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "attack", at = @At("TAIL"))
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        eventBus.post(new AttackEntityEvent(target));
    }

    @Inject(method = "handleContainerInput", at = @At("HEAD"))
    private void onBeforeHandleInput(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (HudManager.petDisplay.isActive()) {
            HudManager.petDisplay.handleInput(slotNum, buttonNum, containerInput);
        }
    }
}
