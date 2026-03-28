package nofrills.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

public class InteractEntityEvent extends Cancellable {
    public Entity entity;
    public EntityHitResult entityHitResult;

    public InteractEntityEvent(Entity entity, EntityHitResult entityHitResult) {
        this.entity = entity;
        this.entityHitResult = entityHitResult;
    }
}
