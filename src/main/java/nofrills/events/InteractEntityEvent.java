package nofrills.events;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;

public class InteractEntityEvent extends Cancellable {
    public Entity entity;
    public EntityHitResult entityHitResult;

    public InteractEntityEvent(Entity entity, EntityHitResult entityHitResult) {
        this.entity = entity;
        this.entityHitResult = entityHitResult;
    }
}
