package nofrills.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import nofrills.features.general.Viewmodel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private float lastEquipProgressMainHand;

    @Shadow
    private float lastEquipProgressOffHand;

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void onBeforeRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            if (!Viewmodel.applyToHand.value() && item.isEmpty()) return;
            if (hand == Hand.MAIN_HAND) {
                matrices.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                matrices.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        if (Viewmodel.instance.isActive()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Viewmodel.rotX.value()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Viewmodel.rotY.value()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Viewmodel.rotZ.value()));
            matrices.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
    private void onBeforeRenderHand(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            if (arm == Arm.RIGHT) {
                matrices.translate(Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            } else {
                matrices.translate(-Viewmodel.offsetX.value(), Viewmodel.offsetY.value(), Viewmodel.offsetZ.value());
            }
        }
    }

    @Inject(method = "renderArmHoldingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderManager;getPlayerRenderer(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/client/render/entity/PlayerEntityRenderer;"))
    private void onRenderHand(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.applyToHand.value()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Viewmodel.rotX.value()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Viewmodel.rotY.value()));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Viewmodel.rotZ.value()));
            matrices.scale((float) Viewmodel.scaleX.value(), (float) Viewmodel.scaleY.value(), (float) Viewmodel.scaleZ.value());
        }
    }

    @Redirect(method = "swingArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void onSwingArmTranslate(MatrixStack instance, float x, float y, float z) {
        if (Viewmodel.instance.isActive()) {
            instance.translate(x * Viewmodel.swingX.value(), y * Viewmodel.swingY.value(), z * Viewmodel.swingZ.value());
        } else {
            instance.translate(x, y, z);
        }
    }

    @Inject(method = "shouldSkipHandAnimationOnSwap", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipAnimation(ItemStack from, ItemStack _to, CallbackInfoReturnable<Boolean> cir) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateHeldItems", at = @At("TAIL"))
    private void onUpdateHeldItems(CallbackInfo ci) {
        if (Viewmodel.instance.isActive() && Viewmodel.noEquip.value()) {
            this.equipProgressMainHand = 1.0f;
            this.equipProgressOffHand = 1.0f;
            this.lastEquipProgressMainHand = 1.0f;
            this.lastEquipProgressOffHand = 1.0f;
        }
    }
}
