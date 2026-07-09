package nofrills.features.tweaks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EventListener;
import nofrills.events.ServerJoinEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventListener
public class LegacyTextures {
    public static final Feature instance = new Feature("legacyTextures", Feature.Flags.UseItemTexturesAPI);
    private static final ConcurrentHashMap<String, ResolvableProfile> cache = new ConcurrentHashMap<>();
    public static SettingBool unlockPackPos = new SettingBool(false, "unlockPackPos", instance);
    public static SettingBool forcePackPos = new SettingBool(false, "forcePackPos", instance);
    public static SettingBool noTooltipStyle = new SettingBool(false, "noTooltipStyle", instance);

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
    private static void onJoin(ServerJoinEvent event) {
        if (!instance.isActive()) {
            cache.clear();
        }
    }
}
