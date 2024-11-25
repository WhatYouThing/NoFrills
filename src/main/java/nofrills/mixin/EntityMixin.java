package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import nofrills.config.Config;
import nofrills.misc.EntityRendering;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
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
    @Shadow
    @Final
    private static int SPRINTING_FLAG_INDEX;
    @Unique
    boolean outlineRender = false;
    @Unique
    private RenderColor outlineColors;
    @Unique
    private boolean filledRender = false;
    @Unique
    private RenderColor filledColors;
    @Unique
    private boolean glowRender = false;
    @Unique
    private RenderColor glowColor;

    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Shadow
    public abstract boolean isTouchingWater();

    @Override
    public void nofrills_mod$setRenderBoxOutline(boolean render, RenderColor color) {
        if (render) {
            outlineColors = color;
        }
        outlineRender = render;
    }

    @Override
    public boolean nofrills_mod$getRenderingOutline() {
        return outlineRender;
    }

    @Override
    public RenderColor nofrills_mod$getOutlineColors() {
        return outlineColors;
    }

    @Override
    public void nofrills_mod$setRenderBoxFilled(boolean render, RenderColor color) {
        if (render) {
            filledColors = color;
        }
        filledRender = render;
    }

    @Override
    public boolean nofrills_mod$getRenderingFilled() {
        return filledRender;
    }

    @Override
    public RenderColor nofrills_mod$getFilledColors() {
        return filledColors;
    }

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
        if (Config.antiSwim && original == EntityPose.SWIMMING) {
            return EntityPose.STANDING;
        }
        return original;
    }

    @ModifyReturnValue(method = "getFlag", at = @At("RETURN"))
    private boolean getFlag(boolean original, int index) {
        if (Config.antiSwim && index == SWIMMING_FLAG_INDEX) {
            return false;
        }
        if (Config.antiSwim && index == SPRINTING_FLAG_INDEX && Utils.isSelf(this)) {
            if (original && isTouchingWater()) {
                setSprinting(false);
                return false;
            }
        }
        return original;
    }

    @Inject(method = "setFlag", at = @At("HEAD"), cancellable = true)
    private void setFlag(int index, boolean value, CallbackInfo ci) {
        if (Config.antiSwim && index == SWIMMING_FLAG_INDEX && value) {
            ci.cancel();
        }
    }
}
