package nofrills.mixin;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FirstPersonHandsAndItems.class)
public interface ItemInHandRendererAccessor {
    @Accessor("mainHandItem")
    void setStack(ItemStack stack);
}
