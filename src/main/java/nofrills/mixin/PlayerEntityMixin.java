package nofrills.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import nofrills.features.tweaks.OldSneak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getBaseDimensions", at = @At("RETURN"))
    private EntityDimensions getDimensions(EntityDimensions original, EntityPose pose) {
        if (OldSneak.active() && pose == EntityPose.CROUCHING) {
            return EntityDimensions.changing(0.6F, 1.8F).withEyeHeight(1.54F);
        }
        return original;
    }
}
