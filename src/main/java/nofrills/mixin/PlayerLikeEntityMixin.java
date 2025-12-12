package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.PlayerLikeEntity;
import nofrills.features.tweaks.OldEyeHeight;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerLikeEntity.class)
public class PlayerLikeEntityMixin {

    @Shadow
    @Final
    public static EntityDimensions STANDING_DIMENSIONS;

    @ModifyReturnValue(method = "getBaseDimensions", at = @At("RETURN"))
    private EntityDimensions getDimensions(EntityDimensions original, EntityPose pose) {
        if (Utils.isSelf(this) && OldEyeHeight.active()) {
            return switch (pose) {
                case CROUCHING -> OldEyeHeight.sneaking.value() ? original.withEyeHeight(1.54f) : original;
                case SWIMMING ->
                        OldEyeHeight.swimming.value() ? original.withEyeHeight(STANDING_DIMENSIONS.eyeHeight()) : original;
                default -> original;
            };
        }
        return original;
    }
}
