package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void onBeforeRenderItem(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            if (!Viewmodel.applyToHand.value() && itemStack.isEmpty()) return;
            if (hand == InteractionHand.MAIN_HAND) {
                poseStack.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                poseStack.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void onRenderItem(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()));
            poseStack.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void onBeforeRenderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            if (arm == HumanoidArm.RIGHT) {
                poseStack.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                poseStack.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderPlayerArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"))
    private void onRenderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()));
            poseStack.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @WrapOperation(method = "swingArm", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0))
    private void onSwingArmTranslate(PoseStack instance, float xo, float yo, float zo, Operation<Void> original) {
        if (Viewmodel.instance.isActive()) {
            original.call(instance, xo * Viewmodel.swingX.valueFloat(), yo * Viewmodel.swingY.valueFloat(), zo * Viewmodel.swingZ.valueFloat());
        } else {
            original.call(instance, xo, yo, zo);
        }
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipAnimation(ItemStack currentlyVisibleItem, ItemStack expectedItem, CallbackInfoReturnable<Boolean> cir) {
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
