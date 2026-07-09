package nofrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import nofrills.features.tweaks.LegacyTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pack.class)
public abstract class PackMixin {

    @Unique
    private boolean nofrills$isHypixelPack() {
        return this.isRequired() && this.getDescription().getString().equals("Hypixel SkyBlock");
    }

    @Shadow
    public abstract Component getDescription();

    @Shadow
    public abstract boolean isRequired();

    @ModifyReturnValue(method = "getDefaultPosition", at = @At("RETURN"))
    private Pack.Position getDefaultPackPos(Pack.Position original) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.forcePackPos.value() && this.nofrills$isHypixelPack()) {
            return Pack.Position.BOTTOM;
        }
        return original;
    }

    @ModifyReturnValue(method = "isFixedPosition", at = @At("RETURN"))
    private boolean isPackFixed(boolean original) {
        if (LegacyTextures.instance.isActive() && LegacyTextures.unlockPackPos.value() && this.nofrills$isHypixelPack()) {
            return false;
        }
        return original;
    }
}
