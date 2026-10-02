package nofrills.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import nofrills.events.AttackEntityEvent;
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
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void beforeDropItem(net.minecraft.client.player.LocalPlayer player, boolean all, CallbackInfo ci) {
        if (nofrills.features.general.ItemProtection.instance.isActive()) {
            if (nofrills.misc.Utils.isInDungeons() && nofrills.misc.DungeonUtil.isDungeonStarted()) return;
            var stack = player.getInventory().getSelectedItem();
            if (!nofrills.features.general.ItemProtection.getProtectType(stack).equals(
                    nofrills.features.general.ItemProtection.ProtectType.None)) ci.cancel();
        }
    }
}
