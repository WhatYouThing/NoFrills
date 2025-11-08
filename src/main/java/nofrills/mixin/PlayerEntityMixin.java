package nofrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import nofrills.features.tweaks.OldSafewalk;
import nofrills.features.tweaks.OldSneak;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    @Final
    public static EntityDimensions STANDING_DIMENSIONS;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getBaseDimensions", at = @At("RETURN"))
    private EntityDimensions getDimensions(EntityDimensions original, EntityPose pose) {
        if (pose == EntityPose.CROUCHING && OldSneak.active()) {
            return STANDING_DIMENSIONS.withEyeHeight(1.54F);
        }
        return original;
    }

    @ModifyExpressionValue(method = "adjustMovementForSneaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStepHeight()F"))
    private float onAdjustMovement(float original) {
        if (OldSafewalk.active()) {
            return 0.95f;
        }
        return original;
    }
}
