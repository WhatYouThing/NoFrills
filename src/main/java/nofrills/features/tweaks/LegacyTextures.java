package nofrills.features.tweaks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.Feature;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;
import nofrills.events.WorldTickEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static nofrills.Main.LOGGER;

@EventListener
public class LegacyTextures {
    public static final Feature instance = new Feature("legacyTextures");

    public static final ConcurrentHashMap<String, ResolvableProfile> cache = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Textures> textures = new ConcurrentHashMap<>();
    public static boolean texturesLoaded = false;

    public static ResolvableProfile getOrInitProfile(String id, String payload) {
        if (!cache.containsKey(id)) {
            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", payload));
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "", new PropertyMap(properties));
            ResolvableProfile profile = ResolvableProfile.createResolved(gameProfile);
            cache.put(id, profile);
        }
        return cache.get(id);
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !texturesLoaded) {
            Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/media/skyblockItemTextures.json").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        textures.put(entry.getKey(), new Textures(
                                object.get("model").getAsString(),
                                object.has("textures") ? object.get("textures").getAsString() : ""
                        ));
                    }
                } catch (Exception exception) {
                    LOGGER.error("Failed to load NoFrills legacy item textures.", exception);
                }
            });
            texturesLoaded = true;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (!instance.isActive()) {
            cache.clear();
            textures.clear();
            texturesLoaded = false;
        }
    }

    public record Textures(String model, String textures) {
    }
}
