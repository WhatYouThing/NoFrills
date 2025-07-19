package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nofrills.features.fixes.EfficiencyFix;
import nofrills.features.fixes.NoDropSwing;
import nofrills.features.general.Viewmodel;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean hasHaste(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noHaste.value() && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean hasMiningFatigue(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noHaste.value() && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "getHandSwingDuration", at = @At("RETURN"))
    private int getSwingSpeed(int original) {
        if (Viewmodel.instance.isActive() && Viewmodel.speed.value() > 0 && Utils.isSelf(this)) {
            return Viewmodel.speed.value();
        }
        return original;
    }

    @Inject(method = "getAttributeValue", at = @At(value = "HEAD"), cancellable = true)
    private void getBreakSpeed(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir) {
        if (EfficiencyFix.active() && Utils.isSelf(this) && attribute.getIdAsString().equals("minecraft:mining_efficiency")) {
            ItemStack stack = Utils.getHeldItem();
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

    @WrapWithCondition(method = "dropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean onDropSwing(LivingEntity instance, Hand hand) {
        if (NoDropSwing.active()) {
            return false;
        }
        return this.getWorld().isClient;
    }
}
