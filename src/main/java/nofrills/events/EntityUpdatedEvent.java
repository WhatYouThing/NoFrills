package nofrills.events;

import net.minecraft.entity.Entity;

public class EntityUpdatedEvent {

    public Entity entity;

    public EntityUpdatedEvent(Entity entity) {
        this.entity = entity;
    }
}
