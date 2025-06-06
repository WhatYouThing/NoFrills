package nofrills.misc;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nofrills.Main.mc;

/**
 * An object for temporarily storing any relevant entity handles, such as armor stands with custom names.
 */
public class EntityCache {
    private final List<Entity> entities = new ArrayList<>();

    public boolean equals(Entity ent1, Entity ent2) {
        return Objects.equals(ent1.getUuidAsString(), ent2.getUuidAsString());
    }

    public boolean has(Entity ent) {
        return entities.stream().anyMatch(entity -> equals(ent, entity));
    }

    public boolean exists(Entity ent) {
        if (mc.world != null && ent != null) {
            Entity ent2 = mc.world.getEntityById(ent.getId());
            return ent2 != null && ent2.isAlive();
        }
        return false;
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
        if (!has(ent)) {
            entities.add(ent);
        }
    }

    public void remove(Entity ent) {
        entities.removeIf(entity -> equals(ent, entity));
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
