package nofrills.events;

import net.minecraft.entity.Entity;

public class EntityRemoveEvent {

    public Entity entity;
    public Entity.RemovalReason reason;

    public EntityRemoveEvent(Entity entity, Entity.RemovalReason reason) {
        this.entity = entity;
        this.reason = reason;
    }
}
