package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import nofrills.features.general.NoRender;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {

    @WrapMethod(method = "submitModel")
    public <S> void onSubmitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, Operation<Void> original) {
        if (NoRender.instance.isActive() && NoRender.entityGlow.value()) {
            original.call(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, sprite, 0, crumblingOverlay);
        } else {
            original.call(model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, sprite, outlineColor, crumblingOverlay);
        }
    }
}
