package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.phys.HitResult;
import nofrills.features.dungeons.WitherDragons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nofrills.Main.eventBus;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(method = "hitTargetOrDeflectSelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;deflection(Lnet/minecraft/world/entity/projectile/Projectile;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    private void onEntityHit(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir, @Local(name = "entity") Entity entity) {
        if ((Projectile) (Object) this instanceof Arrow arrow) {
            eventBus.post(new WitherDragons.ArrowHitEvent(arrow, hitResult, entity));
        }
    }
}
