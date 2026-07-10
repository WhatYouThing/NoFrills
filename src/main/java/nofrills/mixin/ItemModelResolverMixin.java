package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.ItemStack;
import nofrills.features.tweaks.LegacyTextures;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {

    @Shadow
    @Final
    private ModelManager modelManager;

    @ModifyExpressionValue(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;getItemModel(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/item/ItemModel;"))
    private ItemModel getItemModel(ItemModel original, @Local(argsOnly = true, name = "item") LocalRef<ItemStack> item) {
        if (LegacyTextures.instance.isActive()) {
            Optional<LegacyTextures.Replacement> replacement = LegacyTextures.replaceIfNeeded(item.get());
            if (replacement.isPresent()) {
                LegacyTextures.Replacement replaced = replacement.get();
                if (replaced.stack() != null) {
                    item.set(replaced.stack());
                }
                return this.modelManager.getItemModel(replaced.identifier());
            }
        }
        return original;
    }
}
