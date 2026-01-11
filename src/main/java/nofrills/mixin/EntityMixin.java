package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.features.tweaks.HitboxFix;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityRendering {
    @Shadow
    private Vec3d pos;
    @Unique
    private boolean glowRender = false;
    @Unique
    private RenderColor glowColor;

    @Shadow
    public abstract boolean isPlayer();

    @Override
    public void nofrills_mod$setGlowingColored(boolean glowing, RenderColor color) {
        glowRender = glowing;
        glowColor = color;
    }

    @Override
    public boolean nofrills_mod$getGlowing() {
        return glowRender;
    }

    @ModifyReturnValue(method = "isGlowing", at = @At("RETURN"))
    private boolean isGlowing(boolean original) {
        if (glowRender) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method = "getTeamColorValue", at = @At("RETURN"))
    private int getTeamColorValue(int original) {
        if (glowRender) {
            return glowColor.hex;
        }
        return original;
    }

    @ModifyExpressionValue(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"))
    private Box onGetBoundingBox(Box original) {
        if (this.isPlayer() && HitboxFix.active()) {
            return PlayerLikeEntity.STANDING_DIMENSIONS.getBoxAt(this.pos);
        }
        return original;
    }
}
