package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import nofrills.features.tweaks.LegacyTextures;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {

    @Shadow
    @Final
    private ModelManager modelManager;

    @ModifyExpressionValue(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;getItemModel(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/item/ItemModel;"))
    private ItemModel getItemModel(ItemModel original, @Local(argsOnly = true, name = "item") LocalRef<ItemStack> item) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.texturesLoaded) {
            ItemStack stack = item.get();
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            if (model != null && model.getNamespace().equals("hypixel_skyblock")) {
                String id = Utils.getSkyblockId(stack);
                if (!LegacyTextures.textures.containsKey(id)) {
                    return original;
                }
                LegacyTextures.Textures textures = LegacyTextures.textures.get(id);
                if (!textures.textures().isEmpty()) {
                    ItemStack clone = stack.copy();
                    clone.set(DataComponents.PROFILE, LegacyTextures.getOrInitProfile(id, textures.textures()));
                    item.set(clone);
                }
                return this.modelManager.getItemModel(Identifier.parse(textures.model()));
            }
        }
        return original;
    }
}
