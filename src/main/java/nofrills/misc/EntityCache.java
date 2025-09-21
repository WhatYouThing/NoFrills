package nofrills.misc;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        return entities.contains(ent);
    }

    public boolean empty() {
        return entities.isEmpty();
    }

    public int size() {
        return entities.size();
    }

    /**
     * Adds an entity handle to the object. Does nothing if the entity is already on the list.
     */
    public void add(Entity ent) {
        entities.add(ent);
    }

    public void remove(Entity ent) {
        entities.removeIf(ent::equals);
    }

    public void clear() {
        entities.clear();
    }

    public void clearDropped() {
        entities.removeIf(entity -> !exists(entity));
    }

    /**
     * Removes any dead/dropped entities from the list, and returns a copy.
     */
    public List<Entity> get() {
        clearDropped();
        return new ArrayList<>(entities);
    }
}
