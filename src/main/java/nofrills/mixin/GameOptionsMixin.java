package nofrills.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import nofrills.features.general.NoFrontPerspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow
    public abstract void setPerspective(Perspective perspective);

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void onChangePerspective(Perspective perspective, CallbackInfo ci) {
        if (NoFrontPerspective.instance.isActive() && perspective == Perspective.THIRD_PERSON_FRONT) {
            setPerspective(Perspective.FIRST_PERSON);
            ci.cancel();
        }
    }
}
