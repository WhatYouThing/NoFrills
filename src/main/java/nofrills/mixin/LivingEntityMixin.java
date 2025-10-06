package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nofrills.features.general.Fullbright;
import nofrills.features.general.Viewmodel;
import nofrills.features.tweaks.NoDropSwing;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isHolding(Item item);

    @Shadow
    public abstract AttributeContainer getAttributes();

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
        if (Viewmodel.instance.isActive() && Utils.isSelf(this)) {
            if (Viewmodel.noBowSwing.value() && this.isHolding(Items.BOW)) {
                return 0;
            }
            if (Viewmodel.speed.value() > 0) {
                return Viewmodel.speed.value();
            }
        }
        return original;
    }

    @WrapWithCondition(method = "dropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean onDropSwing(LivingEntity instance, Hand hand) {
        if (NoDropSwing.active() && instance == mc.player) {
            return false;
        }
        return this.getWorld().isClient;
    }

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    private boolean hasNightVision(boolean original, RegistryEntry<StatusEffect> effect) {
        if (Fullbright.instance.isActive() && Utils.isSelf(this) && effect == StatusEffects.NIGHT_VISION) {
            if (Fullbright.noEffect.value() && !Fullbright.mode.value().equals(Fullbright.modes.Potion)) {
                return false;
            }
        }
        return original;
    }
}
