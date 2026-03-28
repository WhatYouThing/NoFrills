package nofrills.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.CameraType;
import nofrills.features.tweaks.NoFrontPerspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    public abstract void setCameraType(CameraType perspective);

    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void onChangePerspective(CameraType perspective, CallbackInfo ci) {
        if (NoFrontPerspective.instance.isActive() && perspective == CameraType.THIRD_PERSON_FRONT) {
            setCameraType(CameraType.FIRST_PERSON);
            ci.cancel();
        }
    }
}
