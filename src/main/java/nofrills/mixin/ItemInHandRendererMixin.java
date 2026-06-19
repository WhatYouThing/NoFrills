package nofrills.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import nofrills.features.general.Viewmodel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    private float mainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void onBeforeRenderItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            if (!Viewmodel.applyToHand.value() && item.isEmpty()) return;
            if (hand == InteractionHand.MAIN_HAND) {
                matrices.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                matrices.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void onRenderItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            matrices.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()));
            matrices.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()));
            matrices.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()));
            matrices.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void onBeforeRenderHand(PoseStack matrices, SubmitNodeCollector queue, int light, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            if (arm == HumanoidArm.RIGHT) {
                matrices.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                matrices.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderPlayerArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"))
    private void onRenderHand(PoseStack matrices, SubmitNodeCollector queue, int light, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            matrices.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()));
            matrices.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()));
            matrices.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()));
            matrices.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Redirect(method = "swingArm", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0))
    private void onSwingArmTranslate(PoseStack instance, float x, float y, float z) {
        if (Viewmodel.instance.isActive()) {
            instance.translate(x * Viewmodel.swingX.value(), y * Viewmodel.swingY.value(), z * Viewmodel.swingZ.value());
        } else {
            instance.translate(x, y, z);
        }
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipAnimation(ItemStack from, ItemStack _to, CallbackInfoReturnable<Boolean> cir) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onUpdateHeldItems(CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            this.mainHandHeight = 1.0f;
            this.offHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
        }
    }
}
