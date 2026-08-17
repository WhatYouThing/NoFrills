package nofrills.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.world.entity.Entity;
import nofrills.events.EntityRemovedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

@EventListener
public final class MappedEntityCache<T> {
    private static final CopyOnWriteArrayList<MappedEntityCache<?>> instances = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<Entity, T> entities = new ConcurrentHashMap<>();

    public MappedEntityCache() {
        instances.add(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onRemoved(EntityRemovedEvent event) {
        for (MappedEntityCache<?> instance : instances) {
            instance.remove(event.entity);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onUpdated(EntityUpdatedEvent event) {
        if (event.entity.isRemoved()) {
            for (MappedEntityCache<?> instance : instances) {
                instance.remove(event.entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onJoin(ServerJoinEvent event) {
        for (MappedEntityCache<?> instance : instances) {
            instance.clear();
        }
    }

    public boolean has(Entity ent) {
        return this.entities.containsKey(ent);
    }

    public boolean empty() {
        return this.entities.isEmpty();
    }

    public int size() {
        return this.entities.size();
    }

    public void add(Entity ent, T value) {
        this.entities.put(ent, value);
    }

    public void remove(Entity ent) {
        this.entities.remove(ent);
    }

    public void removeIf(Predicate<Map.Entry<Entity, T>> predicate) {
        this.entities.entrySet().removeIf(predicate);
    }

    public void clear() {
        this.entities.clear();
    }

    public Set<Map.Entry<Entity, T>> get() {
        return this.entities.entrySet();
    }

    public T getValue(Entity entity) {
        return this.getOrDefault(entity, null);
    }

    public T getOrDefault(Entity entity, T defaultValue) {
        return this.entities.getOrDefault(entity, defaultValue);
    }

    public List<Entity> getEntities() {
        return this.entities.keySet().stream().toList();
    }

    public Entity getFirst() {
        return this.entities.keySet().stream().findFirst().orElse(null);
    }
}
