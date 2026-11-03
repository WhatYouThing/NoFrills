package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import nofrills.features.general.Fullbright;
import nofrills.features.general.Viewmodel;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isHolding(Item item);

    @ModifyExpressionValue(method = "getModifiedSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;hasDigSpeed(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean hasHaste(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noHaste.value() && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getModifiedSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean hasMiningFatigue(boolean original) {
        if (Viewmodel.instance.isActive() && Viewmodel.noHaste.value() && Utils.isSelf(this)) {
            return false;
        }
        return original;
    }

    @WrapOperation(method = "swing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity$SwingState;startIfAble(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;I)Z"))
    private boolean onStartSwing(LivingEntity.SwingState instance, InteractionHand hand, SwingAnimation animation, int durationTicks, Operation<Boolean> original) {
        if (Viewmodel.instance.isActive() && Utils.isSelf(this)) {
            if (Viewmodel.noBowSwing.value() && this.isHolding(Items.BOW)) {
                return original.call(instance, hand, animation, 0);
            }
            if (Viewmodel.speed.value() > 0) {
                return original.call(instance, hand, animation, Viewmodel.speed.value());
            }
        }
        return original.call(instance, hand, animation, durationTicks);
    }

    @ModifyReturnValue(method = "hasEffect", at = @At("RETURN"))
    private boolean hasNightVision(boolean original, Holder<MobEffect> effect) {
        if (Fullbright.instance.isActive() && Utils.isSelf(this) && effect == MobEffects.NIGHT_VISION) {
            if (Fullbright.noEffect.value() && !Fullbright.mode.value().equals(Fullbright.Mode.Potion)) {
                return false;
            }
        }
        return original;
    }
}
