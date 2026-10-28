package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import nofrills.config.*;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.EntityCache;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventListener
public class KeyAlert {
    public static final Feature instance = new Feature("dungeonKeyAlert");

    public static final SettingTitle droppedTitle = new SettingTitle("{key} &aDropped", 0, 40, 0, false, "droppedTitle", instance);
    public static final SettingSound droppedSound = new SettingSound(SoundEvents.NOTE_BLOCK_PLING, 1.0, 1.0, "droppedSound", instance);
    public static final SettingTitle obtainedTitle = new SettingTitle("{key} &eObtained", 0, 40, 0, false, "obtainedTitle", instance);
    public static final SettingSound obtainedSound = new SettingSound(SoundEvents.NOTE_BLOCK_PLING, 1.0, 1.0, "obtainedSound", instance);
    public static final SettingBool radius = new SettingBool(false, "radius", instance);
    public static final SettingColor radiusColor = new SettingColor(RenderColor.fromArgb(0xff00ff00), "radiusColor", instance.key());

    private static final Pattern keyNamePattern = Pattern.compile("(?<key>Wither|Blood) Key");
    private static final Pattern keyObtainPattern = Pattern.compile("(?:\\[.*] |)[a-zA-Z0-9_]{3,16} has obtained (?<key>Wither|Blood) Key!");
    private static final EntityCache keyCache = new EntityCache();

    private static String getKeyColor(String key) {
        return switch (key) {
            case "Wither" -> ChatFormatting.DARK_GRAY.toString();
            case "Blood" -> ChatFormatting.RED.toString();
            default -> ChatFormatting.WHITE.toString();
        };
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && !keyCache.has(event.entity) && Utils.isInDungeons()) {
            Matcher matcher = keyNamePattern.matcher(event.namePlain);
            if (!matcher.matches()) return;
            String key = matcher.group("key");
            Utils.showTitle(
                    droppedTitle.title().value().replaceAll("\\{key}", getKeyColor(key) + key + " Key"),
                    "",
                    droppedTitle.fadeIn().value(),
                    droppedTitle.stay().value(),
                    droppedTitle.fadeOut().value(),
                    droppedTitle.vanilla().value()
            );
            Utils.playSound(droppedSound);
            keyCache.add(event.entity);
        }
    }

    @EventHandler
    private static void onMsg(ChatMsgEvent event) {
        if (instance.isActive() && !keyCache.empty() && Utils.isInDungeons()) {
            Matcher matcher = keyObtainPattern.matcher(event.msg());
            if (!matcher.matches()) return;
            String key = matcher.group("key");
            Utils.showTitle(
                    obtainedTitle.title().value().replaceAll("\\{key}", getKeyColor(key) + key + " Key"),
                    "",
                    obtainedTitle.fadeIn().value(),
                    obtainedTitle.stay().value(),
                    obtainedTitle.fadeOut().value(),
                    obtainedTitle.vanilla().value()
            );
            Utils.playSound(obtainedSound);
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && radius.value() && !keyCache.empty()) {
            for (Entity key : keyCache.get()) {
                event.drawCircle(key.getEyePosition(), 6.0, false, radiusColor.value());
            }
        }
    }
}
