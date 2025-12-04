package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import nofrills.features.tweaks.AntiSwim;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityRendering {
    @Shadow
    @Final
    private static int SWIMMING_FLAG_INDEX;
    @Unique
    private boolean glowRender = false;
    @Unique
    private RenderColor glowColor;

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

    @ModifyReturnValue(method = "getPose", at = @At("RETURN"))
    private EntityPose getPose(EntityPose original) {
        if (original == EntityPose.SWIMMING && AntiSwim.active()) {
            return EntityPose.STANDING;
        }
        return original;
    }

    @ModifyReturnValue(method = "getFlag", at = @At("RETURN"))
    private boolean getFlag(boolean original, int index) {
        if (index == SWIMMING_FLAG_INDEX && AntiSwim.active()) {
            return false;
        }
        return original;
    }

    @Inject(method = "setFlag", at = @At("HEAD"), cancellable = true)
    private void setFlag(int index, boolean value, CallbackInfo ci) {
        if (index == SWIMMING_FLAG_INDEX && value && AntiSwim.active()) {
            ci.cancel();
        }
    }
}
