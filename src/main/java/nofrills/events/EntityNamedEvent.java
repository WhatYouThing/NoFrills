package nofrills.events;

import net.minecraft.entity.Entity;

public class EntityNamedEvent {

    public Entity entity;
    public String namePlain;

    public EntityNamedEvent(Entity entity, String namePlain) {
        this.entity = entity;
        this.namePlain = namePlain;
    }
}
