package nofrills.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import nofrills.events.AttackEntityEvent;
import nofrills.features.general.ItemProtection;
import nofrills.hud.HudManager;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;
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

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void onBeforeDropItem(LocalPlayer player, boolean all, CallbackInfo ci) {
        if (ItemProtection.instance.isActive()) {
            if (Utils.isInDungeons() && DungeonUtil.isDungeonStarted()) {
                return; // items cannot be directly dropped while in an active dungeon due to the class ability
            }
            ItemStack stack = player.getInventory().getSelectedItem();
            if (!ItemProtection.getProtectType(stack).equals(ItemProtection.ProtectType.None)) {
                ci.cancel();
            }
        }
    }
}
