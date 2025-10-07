package nofrills.misc;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import static nofrills.Main.mc;

/**
 * An object for temporarily storing any relevant entity handles, such as armor stands with custom names.
 */
public class EntityCache {
    private final HashSet<Entity> entities = new HashSet<>();

    public static boolean exists(Entity ent) {
        return ent != null && ent.isAlive() && mc.world != null && mc.world.getEntityById(ent.getId()) != null;
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

    public void removeDead() {
        this.entities.removeIf(entity -> !exists(entity));
    }

    public void removeIf(Predicate<Entity> filter) {
        this.entities.removeIf(filter);
    }

    /**
     * Removes any dead/dropped entities from the list, and returns a copy.
     */
    public List<Entity> get() {
        this.removeDead();
        return new ArrayList<>(this.entities);
    }
}
