package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.PlayerLikeEntity;
import nofrills.features.tweaks.OldSneak;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerLikeEntity.class)
public class PlayerLikeEntityMixin {

    @Shadow
    @Final
    protected static EntityDimensions STANDING_DIMENSIONS;

    @ModifyReturnValue(method = "getBaseDimensions", at = @At("RETURN"))
    private EntityDimensions getDimensions(EntityDimensions original, EntityPose pose) {
        if (pose == EntityPose.CROUCHING && OldSneak.active()) {
            return STANDING_DIMENSIONS.withEyeHeight(1.54F);
        }
        return original;
    }
}
