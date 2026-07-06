package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import nofrills.features.tweaks.LegacyTextures;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static nofrills.Main.mc;

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
                CompoundTag data = Utils.getCustomData(stack);
                String id = Utils.getSkyblockId(data);
                if (id.isEmpty() || !LegacyTextures.textures.containsKey(id)) {
                    return original;
                }
                if (id.equals("VOIDEDGE_KATANA") || id.equals("VORPAL_KATANA") || id.equals("ATOMSPLIT_KATANA")) {
                    return this.modelManager.getItemModel(Identifier.withDefaultNamespace(
                            mc.player.getCooldowns().isOnCooldown(stack) ? "golden_sword" : "diamond_sword"
                    ));
                }
                if (data.contains("td_attune_mode")) {
                    String path = switch (data.getIntOr("td_attune_mode", -1)) {
                        case 0 -> "stone_sword";
                        case 1 -> "golden_sword";
                        case 2 -> "iron_sword";
                        case 3 -> "diamond_sword";
                        default -> "";
                    };
                    if (!path.isEmpty()) return this.modelManager.getItemModel(Identifier.withDefaultNamespace(path));
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
