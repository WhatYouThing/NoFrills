package nofrills.misc;

import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.function.Predicate;

/**
 * An object for temporarily storing any relevant entity handles, such as armor stands with custom names.
 */
public final class EntityCache {

    private static final Object present = new Object();
    private final MappedEntityCache<Object> cache = new MappedEntityCache<>();

    public EntityCache() {
    }

    public boolean has(Entity ent) {
        return this.cache.has(ent);
    }

    public boolean empty() {
        return this.cache.empty();
    }

    public int size() {
        return this.cache.size();
    }

    /**
     * Adds an entity handle to the object. Does nothing if the entity is already on the list.
     */
    public void add(Entity ent) {
        this.cache.add(ent, present);
    }

    /**
     * Removes an entity handle from the object. Does nothing if the entity is not on the list.
     */
    public void remove(Entity ent) {
        this.cache.remove(ent);
    }

    public void removeIf(Predicate<Entity> predicate) {
        this.cache.removeIf(entry -> predicate.test(entry.getKey()));
    }

    public void clear() {
        this.cache.clear();
    }

    public List<Entity> get() {
        return this.cache.getEntities();
    }

    public Entity getFirst() {
        return this.cache.getFirst();
    }
}
