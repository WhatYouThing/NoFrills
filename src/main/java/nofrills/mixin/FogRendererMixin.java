package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import nofrills.features.general.NoRender;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    @Final
    private static List<FogModifier> FOG_MODIFIERS;

    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/fog/FogData;renderDistanceEnd:F", shift = At.Shift.AFTER, ordinal = 0))
    private void onGetFocused(Camera camera, int viewDistance, RenderTickCounter renderTickCounter, float f, ClientWorld clientWorld, CallbackInfoReturnable<Vector4f> cir, @Local LocalRef<FogData> fogRef) {
        if (NoRender.instance.isActive() && NoRender.fog.value()) {
            CameraSubmersionType type = camera.getSubmersionType();
            Entity entity = camera.getFocusedEntity();
            for (FogModifier modifier : FOG_MODIFIERS) {
                if (modifier.shouldApply(type, entity)) {
                    return; // do nothing if a modifier is active (submerged in lava, blindness etc.)
                }
            }
            fogRef.set(NoRender.getFogAsEmpty(fogRef.get()));
        }
    }
}
