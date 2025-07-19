package nofrills.mixin;

import com.google.common.collect.Lists;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import nofrills.features.general.NoFrontPerspective;
import nofrills.features.general.SlotBinding;
import nofrills.features.keybinds.PearlRefill;
import nofrills.features.keybinds.RecipeLookup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Mutable
    @Shadow
    @Final
    public KeyBinding[] allKeys;

    @Shadow
    public abstract void setPerspective(Perspective perspective);

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void onChangePerspective(Perspective perspective, CallbackInfo ci) {
        if (NoFrontPerspective.instance.isActive() && perspective == Perspective.THIRD_PERSON_FRONT) {
            setPerspective(Perspective.FIRST_PERSON);
            ci.cancel();
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void onLoadKeybinding(CallbackInfo ci) {
        List<KeyBinding> binds = Lists.newArrayList(
                SlotBinding.bind,
                RecipeLookup.bind,
                PearlRefill.bind
        );
        List<KeyBinding> keys = Lists.newArrayList(this.allKeys);
        keys.addAll(binds);
        this.allKeys = keys.toArray(KeyBinding[]::new);
    }
}
