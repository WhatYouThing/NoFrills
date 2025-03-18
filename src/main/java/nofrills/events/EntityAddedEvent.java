package nofrills.events;

import net.minecraft.entity.Entity;

public class EntityAddedEvent {

    public Entity entity;

    public EntityAddedEvent(Entity entity) {
        this.entity = entity;
    }
}
