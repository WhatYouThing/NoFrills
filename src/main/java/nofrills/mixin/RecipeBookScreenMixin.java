package nofrills.mixin;

import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import nofrills.features.general.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookScreen.class)
public abstract class RecipeBookScreenMixin {

    @Inject(method = "addRecipeBook", at = @At("HEAD"), cancellable = true)
    private void onPlaceBlock(CallbackInfo ci) {
        if (NoRender.instance.isActive() && NoRender.recipeBook.value()) {
            ci.cancel();
        }
    }
}