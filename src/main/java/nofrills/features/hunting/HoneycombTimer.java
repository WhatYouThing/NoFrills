package nofrills.features.hunting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.events.EventListener;
import nofrills.misc.RenderColor;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static nofrills.Main.mc;

@EventListener
public class HoneycombTimer {
    public static final Feature instance = new Feature("honeycombTimer");

    public static final SettingColor color = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN).withAlpha(0.33f), "color", instance);
    public static final SettingDouble scale = new SettingDouble(0.5, "scale", instance);

    private static final DataFile data = Config.getDataFile("HoneycombTimer.json");
    private static List<Honeycomb> activeHoneycombs = new ArrayList<>();

    private static BlockPos getHoneycombPos(JsonElement element) {
        JsonObject honeycomb = element.getAsJsonObject();
        return new BlockPos(honeycomb.get("x").getAsInt(), honeycomb.get("y").getAsInt(), honeycomb.get("z").getAsInt());
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && Utils.isInGalatea() && event.namePlain.startsWith(Utils.Symbols.critter + " Critter in: ")) {
            if (!data.get().has("waypoints")) {
                data.get().add("waypoints", new JsonArray());
            }
            JsonArray waypoints = data.get().get("waypoints").getAsJsonArray();
            BlockPos ground = Utils.findGround(BlockPos.containing(event.entity.position()), 6);
            String timeString = event.namePlain.substring(event.namePlain.indexOf(":") + 2);
            if (timeString.equals("1s")) {
                return; // the time sits at 1 second when the tree is approached after the honeycomb is done
            }
            Calendar time = Utils.parseTime(timeString);
            Optional<JsonElement> element = waypoints.asList().stream().filter(e -> getHoneycombPos(e).equals(ground)).findFirst();
            if (element.isPresent()) {
                JsonObject object = element.get().getAsJsonObject();
                object.addProperty("timestamp", time.getTimeInMillis());
            } else {
                JsonObject object = new JsonObject();
                object.addProperty("timestamp", time.getTimeInMillis());
                object.addProperty("area", SkyblockData.getArea());
                object.addProperty("x", ground.getX());
                object.addProperty("y", ground.getY());
                object.addProperty("z", ground.getZ());
                object.addProperty("done", false);
                waypoints.add(object);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.player != null && Utils.isInSkyblock() && data.get().has("waypoints")) {
            List<JsonElement> waypoints = data.get().get("waypoints").getAsJsonArray().asList();
            waypoints.removeIf(e -> {
                JsonObject waypoint = e.getAsJsonObject();
                return waypoint.get("done").getAsBoolean() && Vec3.atCenterOf(getHoneycombPos(waypoint)).distanceTo(mc.player.position()) <= 10.0;
            });
            Set<String> areas = waypoints.stream().map(e -> e.getAsJsonObject().get("area").getAsString()).collect(Collectors.toSet());
            List<Honeycomb> list = new ArrayList<>();
            long now = Instant.now().toEpochMilli();
            for (String area : areas) {
                List<JsonElement> areaWaypoints = waypoints.stream()
                        .filter(e -> e.getAsJsonObject().get("area").getAsString().equals(area)).toList();
                for (int i = 0; i < areaWaypoints.size(); i++) {
                    JsonObject waypoint = areaWaypoints.get(i).getAsJsonObject();
                    long timestamp = waypoint.get("timestamp").getAsLong();
                    if (timestamp <= now && !waypoint.get("done").getAsBoolean()) {
                        String msg = Utils.format("Honeycomb #{} on island {} is now ready.", i + 1, area);
                        Utils.infoRaw(Component.literal(msg).withStyle(ChatFormatting.GREEN));
                        waypoint.addProperty("done", true);
                    }
                    if (area.equals(SkyblockData.getArea())) {
                        list.add(new Honeycomb(getHoneycombPos(waypoint), timestamp, area));
                    }
                }
            }
            activeHoneycombs = list;
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !activeHoneycombs.isEmpty()) {
            List<Honeycomb> honeycombs = activeHoneycombs;
            long timestamp = Instant.now().toEpochMilli();
            for (Honeycomb honeycomb : honeycombs) {
                long timeLeft = honeycomb.timestamp() - timestamp;
                String text = Utils.format("{}#{}: {}",
                        Utils.getPercentageColor(timeLeft / 3600000.0),
                        honeycombs.indexOf(honeycomb) + 1,
                        timeLeft <= 0 ? "Ready" : Utils.millisecondsToTime(timeLeft)
                );
                event.drawBeam(Vec3.atCenterOf(honeycomb.pos()).add(0.0, 0.5, 0.0), 256, true, color.value());
                event.drawDistanceScaledText(
                        Vec3.atCenterOf(honeycomb.pos()),
                        Component.literal(text),
                        scale.valueFloat() * 0.1f,
                        true,
                        RenderColor.WHITE
                );
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        activeHoneycombs = new ArrayList<>();
    }

    public record Honeycomb(BlockPos pos, long timestamp, String area) {
    }
}
