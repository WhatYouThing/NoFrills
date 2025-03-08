package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import nofrills.config.Config;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract AttributeContainer getAttributes();

    @ModifyExpressionValue(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean onTravel(boolean original) {
        if (Utils.isFixEnabled(Config.antiSwim)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "applyFluidMovingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean onApplyFluidSpeed(boolean original) {
        if (Utils.isFixEnabled(Config.antiSwim)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean hasHaste(boolean original) {
        if (Config.noHaste && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean hasMiningFatigue(boolean original) {
        if (Config.noHaste && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
    private int getSwingSpeed(int original) {
        if (Config.viewmodelSpeed != 0 && Utils.isSelf(this)) {
            return Config.viewmodelSpeed;
        }
        return original;
    }

    @Inject(method = "getAttributeValue", at = @At(value = "HEAD"), cancellable = true)
    private void getBreakSpeed(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir) {
        if (Utils.isFixEnabled(Config.efficiencyFix) && Utils.isSelf(this) && attribute.getIdAsString().equals("minecraft:mining_efficiency")) {
            ItemStack stack = mc.player.getMainHandStack();
            ItemEnchantmentsComponent enchants = stack.getComponents().get(DataComponentTypes.ENCHANTMENTS);
            if (enchants != null) {
                for (RegistryEntry<Enchantment> enchant : enchants.getEnchantments()) {
                    if (enchant.getIdAsString().equals("minecraft:efficiency")) {
                        cir.setReturnValue(Math.pow(enchants.getLevel(enchant), 2) + 1);
                    }
                }
            }
        }
    }
}
