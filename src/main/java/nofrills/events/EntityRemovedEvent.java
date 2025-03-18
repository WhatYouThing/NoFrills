package nofrills.events;

import net.minecraft.entity.Entity;

public class EntityRemovedEvent {
    public Entity entity;
    public Entity.RemovalReason reason;

    public EntityRemovedEvent(Entity entity, Entity.RemovalReason reason) {
        this.entity = entity;
        this.reason = reason;
    }
}