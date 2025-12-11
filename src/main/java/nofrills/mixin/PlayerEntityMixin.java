package nofrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import nofrills.features.tweaks.OldEyeHeight;
import nofrills.misc.Utils;
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
        if (Utils.isSelf(this) && OldEyeHeight.active()) {
            return switch (pose) {
                case CROUCHING -> original.withEyeHeight(1.54f);
                case SWIMMING -> original.withEyeHeight(STANDING_DIMENSIONS.eyeHeight());
                default -> original;
            };
        }
        return original;
    }
}
