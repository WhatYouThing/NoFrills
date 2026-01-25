package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import nofrills.events.EntityRemovedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.ServerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import static nofrills.Main.mc;

/**
 * An object for temporarily storing any relevant entity handles, such as armor stands with custom names.
 */
public class EntityCache {
    private static final List<EntityCache> instances = new ArrayList<>();

    private final CopyOnWriteArraySet<Entity> entities = new CopyOnWriteArraySet<>();

    public EntityCache() {
        instances.add(this);
    }

    public static boolean exists(Entity ent) {
        return ent != null && mc.world != null && ent.isAlive() && mc.world.getEntityById(ent.getId()) != null;
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onRemoved(EntityRemovedEvent event) {
        for (EntityCache instance : instances) {
            instance.remove(event.entity);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onUpdated(EntityUpdatedEvent event) {
        if (event.entity.isRemoved()) {
            for (EntityCache instance : instances) {
                instance.remove(event.entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onJoin(ServerJoinEvent event) {
        for (EntityCache instance : instances) {
            instance.clear();
        }
    }

    public boolean equals(Entity ent1, Entity ent2) {
        return ent1.equals(ent2);
    }

    public boolean has(Entity ent) {
        return this.entities.contains(ent);
    }

    public boolean empty() {
        return this.entities.isEmpty();
    }

    public int size() {
        return this.entities.size();
    }

    /**
     * Adds an entity handle to the object. Does nothing if the entity is already on the list.
     */
    public void add(Entity ent) {
        this.entities.add(ent);
    }

    /**
     * Removes an entity handle from the object. Does nothing if the entity is not on the list.
     */
    public void remove(Entity ent) {
        this.entities.remove(ent);
    }

    public void clear() {
        this.entities.clear();
    }

    public CopyOnWriteArraySet<Entity> get() {
        return this.entities;
    }
}
