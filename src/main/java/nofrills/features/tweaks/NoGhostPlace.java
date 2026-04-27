package nofrills.features.tweaks;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.ConcurrentHashSet;
import nofrills.misc.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;

import static nofrills.Main.LOGGER;

public class NoGhostPlace {
    public static final Feature instance = new Feature("noGhostPlace");

    private static final ConcurrentHashSet<String> items = new ConcurrentHashSet<>();
    private static final HashSet<String> abilityWhitelist = Sets.newHashSet(
            "ABINGOPHONE",
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "SNOW_CANNON",
            "SNOW_BLASTER",
            "SNOW_HOWITZER"
    );

    public static boolean isNonPlaceable(BlockPlaceContext context) {
        if (context != null) {
            ItemStack stack = context.getItemInHand();
            String id = Utils.getSkyblockId(stack);
            if (stack.getItem() instanceof BlockItem && !id.isEmpty()) {
                return items.contains(id) || abilityWhitelist.contains(id) || id.startsWith("ABIPHONE") || Utils.hasRightClickAbility(stack);
            }
        }
        return false;
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive() && items.isEmpty()) {
            Thread.startVirtualThread(() -> {
                try {
                    InputStream connection = URI.create("https://whatyouth.ing/api/nofrills/v1/misc/get-item-attributes/").toURL().openStream();
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(connection)).getAsJsonObject();
                    for (JsonElement element : json.get("non_placeable").getAsJsonArray()) {
                        items.add(element.getAsString());
                    }
                } catch (Exception exception) {
                    LOGGER.error("Failed to refresh item attributes from NoFrills API.", exception);
                }
            });
        }
    }
}