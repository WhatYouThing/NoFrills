package nofrills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import nofrills.features.general.Viewmodel;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V", shift = At.Shift.AFTER))
    private void onBeforeRenderItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            if (!Viewmodel.applyToHand.value() && itemStack.isEmpty()) return;
            if (hand == InteractionHand.MAIN_HAND) {
                poseStack.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                poseStack.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void onRenderItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()).get(new Matrix4f()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()).get(new Matrix4f()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()).get(new Matrix4f()));
            poseStack.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void onBeforeRenderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, PlayerRenderState playerState, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            if (arm == HumanoidArm.RIGHT) {
                poseStack.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                poseStack.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderPlayerArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;renderPlayerHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;)V"))
    private void onRenderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, PlayerRenderState playerState, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Viewmodel.rotX.value()).get(new Matrix4f()));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Viewmodel.rotY.value()).get(new Matrix4f()));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Viewmodel.rotZ.value()).get(new Matrix4f()));
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
}
