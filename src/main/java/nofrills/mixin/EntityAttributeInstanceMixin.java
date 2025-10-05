package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import nofrills.features.tweaks.EnchantFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin {
    @Shadow
    @Final
    private RegistryEntry<EntityAttribute> type;

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double onGetValue(double original) {
        if (EnchantFix.active()) {
            if (this.type.equals(EntityAttributes.MINING_EFFICIENCY)) {
                return EnchantFix.getEfficiencyValue();
            }
            if (this.type.equals(EntityAttributes.SUBMERGED_MINING_SPEED)) {
                return EnchantFix.getAquaAffinityValue();
            }
        }
        return original;
    }
}
