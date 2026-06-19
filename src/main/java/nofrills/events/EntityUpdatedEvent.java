package nofrills.events;

import net.minecraft.world.entity.Entity;

public class EntityUpdatedEvent {

    public Entity entity;

    public EntityUpdatedEvent(Entity entity) {
        this.entity = entity;
    }
}
