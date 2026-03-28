package nofrills.events;

import net.minecraft.world.entity.Entity;

public class AttackEntityEvent {
    public Entity entity;

    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }
}
