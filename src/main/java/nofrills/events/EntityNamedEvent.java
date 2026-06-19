package nofrills.events;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import nofrills.misc.Utils;

public class EntityNamedEvent {
    public Entity entity;
    public Component name;
    public String namePlain;

    public EntityNamedEvent(Entity entity, Component name) {
        this.entity = entity;
        this.name = name;
        this.namePlain = Utils.toPlain(name);
    }
}
